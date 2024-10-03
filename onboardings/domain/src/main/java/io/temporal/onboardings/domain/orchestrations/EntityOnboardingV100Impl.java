/*
 * MIT License
 *
 * Copyright (c) 2024 temporal.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.temporal.onboardings.domain.orchestrations;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.failure.ActivityFailure;
import io.temporal.failure.ApplicationFailure;
import io.temporal.onboardings.domain.integrations.IntegrationsHandlers;
import io.temporal.onboardings.domain.messages.commands.ApproveEntityRequest;
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

public class EntityOnboardingV100Impl implements EntityOnboarding {
  Logger logger = Workflow.getLogger(EntityOnboardingV100Impl.class);
  private EntityOnboardingState state;
  private final IntegrationsHandlers integrationsHandlers =
      Workflow.newActivityStub(
          IntegrationsHandlers.class,
          ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build());
  private final NotificationsHandlers notificationHandlers =
      Workflow.newActivityStub(
          NotificationsHandlers.class,
          ActivityOptions.newBuilder()
              .setRetryOptions(
                  RetryOptions.newBuilder()
                      .
                      // Since we are delivering an message, we want to restrict the number of retry
                      // attempts we make
                      // lest we inadvertently build a SPAM server.
                      setMaximumAttempts(2)
                      .build())
              .setStartToCloseTimeout(Duration.ofSeconds(2))
              .build());

  @Override
  public void execute(OnboardEntityRequest args) {
    state =
        new EntityOnboardingState(
            args.id(),
            args.value(),
            args.skipApproval()
                ? new Approval(ApprovalStatus.APPROVED, null)
                : new Approval(ApprovalStatus.PENDING, null));
    var notifyDeputyOwner = args.deputyOwnerEmail() != null && !args.deputyOwnerEmail().isEmpty();
    assertValidArgs(args);
    if (!args.skipApproval()) {
      var waitApprovalSecs = args.completionTimeoutSeconds();
      if (notifyDeputyOwner) {
        // We lean into integer division here to be unconcerned about
        // determinism issues. Note that if we did this with a float/double
        // we could run into a problem with hardware results and violate the determinism
        // requirement for our Timer.
        waitApprovalSecs = waitApprovalSecs / 2;
      }

      // this blocks until we flip the `ApprovalStatus` bit on our state object
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
                args.id(),
                state.currentValue(),
                args.completionTimeoutSeconds() - waitApprovalSecs,
                null,
                args.skipApproval());
        can.execute(canArgs);
        return;
      }
    }

    // make sure we are APPROVED to proceed with the Onboarding
    if (!state.approval().approvalStatus().equals(ApprovalStatus.APPROVED)) {
      logger.info("Entity was rejected for {}", args.id());
      return;
    }

    try {
      //      integrationsHandlers.registerCrmEntity(new RegisterCrmEntityRequest(args.id(),
      // args.value()));
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
      /*
       * Temporal is not prescriptive about the strategy you choose for indicating failures in your Workflows.
       *
       * We throw an ApplicationFailureException here which would ultimately result in a `WorkflowFailedException`.
       * This is a common way to fail a Workflow which will never succeed due to bad arguments or some other invariant.
       *
       * It is common to use ApplicationFailure for business failures, but these should be considered distinct from an intermittent failure such as
       * a bug in the code or some dependency which is temporarily unavailable. Temporal can often recover from these kinds of intermittent failures
       * with a redeployment, downstream service correction, etc. These intermittent failures would typically result in an Exception NOT descended from
       * TemporalFailure and would therefore NOT fail the Workflow Execution.
       *
       * If you have explicit business metrics setup to monitor failed Workflows, you could alternatively return a "Status" result with the business failure
       * and allow the Workflow Execution to "Complete" without failure.
       *
       * Note that `WorkflowFailedException` will count towards the `workflow_failed` SDK Metric (https://docs.temporal.io/references/sdk-metrics#workflow_failed).
       */
      throw ApplicationFailure.newFailure("id and value are required", Errors.INVALID_ARGS.name());
    }
  }
}
