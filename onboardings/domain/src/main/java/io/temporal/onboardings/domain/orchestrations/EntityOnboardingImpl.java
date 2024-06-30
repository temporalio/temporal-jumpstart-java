package io.temporal.onboardings.domain.orchestrations;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.failure.ActivityFailure;
import io.temporal.failure.ApplicationFailure;
import io.temporal.onboardings.domain.integrations.IntegrationsHandlers;
import io.temporal.onboardings.domain.messages.commands.ApproveEntityRequest;
import io.temporal.onboardings.domain.messages.commands.RegisterCrmEntityRequest;
import io.temporal.onboardings.domain.messages.commands.RejectEntityRequest;
import io.temporal.onboardings.domain.messages.commands.RequestDeputyOwnerApprovalRequest;
import io.temporal.onboardings.domain.messages.orchestrations.Errors;
import io.temporal.onboardings.domain.messages.orchestrations.OnboardEntityRequest;
import io.temporal.onboardings.domain.messages.queries.EntityOnboardingState;
import io.temporal.onboardings.domain.messages.values.Approval;
import io.temporal.onboardings.domain.messages.values.ApprovalStatus;
import io.temporal.onboardings.domain.notifications.NotificationsHandlers;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import org.slf4j.Logger;

public class EntityOnboardingImpl implements EntityOnboarding {
  Logger logger = Workflow.getLogger(EntityOnboardingImpl.class);
  private EntityOnboardingState state;
  private final IntegrationsHandlers integrationsHandlers =
      Workflow.newActivityStub(
          IntegrationsHandlers.class,
          ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build());
  private final NotificationsHandlers notificationHandlers =
      Workflow.newActivityStub(
          NotificationsHandlers.class,
          ActivityOptions.newBuilder()
              .setRetryOptions(RetryOptions.newBuilder().setMaximumAttempts(2).build())
              .setStartToCloseTimeout(Duration.ofSeconds(2))
              .build());

  @Override
  public void execute(OnboardEntityRequest args) {
    state =
        new EntityOnboardingState(
            args.id(), args.value(), new Approval(ApprovalStatus.PENDING, null));
    var notifyDeputyOwner = args.deputyOwnerEmail() != null && !args.deputyOwnerEmail().isEmpty();
    assertValidArgs(args);
    if (!args.skipApproval()) {
      var waitApprovalSecs = args.completionTimeoutSeconds();
      if (notifyDeputyOwner) {
        waitApprovalSecs = waitApprovalSecs / 2;
      }
      var conditionMet =
          Workflow.await(
              Duration.ofSeconds(waitApprovalSecs),
              () -> !state.approval().approvalStatus().equals(ApprovalStatus.PENDING));
      if (!conditionMet) {
        if (!notifyDeputyOwner) {
          throw ApplicationFailure.newFailure(
              String.format("Never received approval for %s", args.id()),
              Errors.ONBOARD_ENTITY_TIMED_OUT.name());
        }
        notificationHandlers.requestDeputyOwnerApproval(
            new RequestDeputyOwnerApprovalRequest(args.id(), args.deputyOwnerEmail()));
        var can = Workflow.newContinueAsNewStub(EntityOnboarding.class);
        var canArgs =
            new OnboardEntityRequest(
                args.id(), state.currentValue(), waitApprovalSecs, null, args.skipApproval());
        can.execute(canArgs);
        return;
      }
    }
    if (!state.approval().approvalStatus().equals(ApprovalStatus.APPROVED)) {
      return;
    }

    try {
      integrationsHandlers.registerCrmEntity(new RegisterCrmEntityRequest(args.id(), args.value()));
    } catch (ActivityFailure e) {
      ApplicationFailure af = (ApplicationFailure) e.getCause();
      if (af.isNonRetryable()) {
        logger.info("Non-retryable activity: {}", af.getType());
      }
      throw e;
    }
  }

  @Override
  public EntityOnboardingState getState() {
    return state;
  }

  @Override
  public void approve(ApproveEntityRequest cmd) {
    state =
        new EntityOnboardingState(
            state.id(), state.currentValue(), new Approval(ApprovalStatus.APPROVED, cmd.comment()));
  }

  @Override
  public void reject(RejectEntityRequest cmd) {
    state =
        new EntityOnboardingState(
            state.id(), state.currentValue(), new Approval(ApprovalStatus.REJECTED, cmd.comment()));
  }

  private void assertValidArgs(OnboardEntityRequest args) {
    if (args.id() == null
        || args.id().isEmpty()
        || args.value() == null
        || args.value().isEmpty()) {
      throw ApplicationFailure.newFailure("id and value are required", "invalid_args");
    }
  }
}
