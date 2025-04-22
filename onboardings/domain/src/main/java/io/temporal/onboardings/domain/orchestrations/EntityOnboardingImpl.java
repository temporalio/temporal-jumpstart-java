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
import io.temporal.onboardings.domain.messages.commands.*;
import io.temporal.onboardings.domain.messages.orchestrations.Errors;
import io.temporal.onboardings.domain.messages.orchestrations.OnboardEntityRequest;
import io.temporal.onboardings.domain.messages.queries.EntityOnboardingState;
import io.temporal.onboardings.domain.messages.values.Approval;
import io.temporal.onboardings.domain.messages.values.ApprovalStatus;
import io.temporal.onboardings.domain.notifications.NotificationsHandlers;
import io.temporal.workflow.CancellationScope;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInit;
import java.time.Duration;
import java.util.Objects;
import javax.annotation.Nullable;
import org.slf4j.Logger;

// EntityOnboardingImpl is always the `latest` implementation of `EntityOnboarding`
// Versions being superceded will be copied to a separate file (or package), incremented by
// The format {WorkflowInterface}V{previousVersion + 1}Impl
// example:
// 1. The version before `latest` is `EntityOnboardingV99Impl` version
// 2. Copy current `latest` EntityOnboardingImpl as `EntityOnboardingV100Impl`
// 3. Make changes in the existing `EntityOnboardingImpl` with appropriate GetVersion calls
public class EntityOnboardingImpl implements EntityOnboarding {
  Logger logger = Workflow.getLogger(EntityOnboardingImpl.class);
  private EntityOnboardingState state;
  private boolean shouldSync;
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

  @WorkflowInit
  public EntityOnboardingImpl(@Nullable OnboardEntityRequest args) {
    // Initialize the state object with
    // a non null object in the event of signal/update arriving soon
    // See https://docs.temporal.io/handling-messages for details
    init(args);
  }

  private void init(@Nullable OnboardEntityRequest args) {
    var status =
        args.skipApproval()
            ? new Approval(ApprovalStatus.APPROVED, null)
            : new Approval(ApprovalStatus.PENDING, null);
    this.state = new EntityOnboardingState(args.id(), args.value(), status);
  }

  @Override
  public void execute(OnboardEntityRequest args) {
    var notifyDeputyOwner =
        Objects.nonNull(args.deputyOwnerEmail()) && !args.deputyOwnerEmail().isEmpty();

    assertValidArgs(args);

    // Sync the Things
    // THIS WILL RAISE A NDE!
    // Run replay test to see how that looks
    CancellationScope syncScope = startSyncToStorage();

    syncScope.run();

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
                false);
        // be sure to check that all handlers have been completed before CAN
        Workflow.await(Workflow::isEveryHandlerFinished);
        can.execute(canArgs);
        return;
      }
    }

    // make sure we are APPROVED to proceed with the Onboarding
    if (!state.approval().approvalStatus().equals(ApprovalStatus.APPROVED)) {
      Workflow.await(Workflow::isEveryHandlerFinished);
      logger.info("Entity was rejected for {}", args.id());
      return;
    }

    try {
      if (Workflow.getVersion("REGISTER_CRM_ENTITY", Workflow.DEFAULT_VERSION, 1)
          != Workflow.DEFAULT_VERSION) {
        integrationsHandlers.registerCrmEntity(
            new RegisterCrmEntityRequest(args.id(), args.value()));
      }
    } catch (ActivityFailure e) {
      ApplicationFailure af = (ApplicationFailure) e.getCause();
      if (af.isNonRetryable()) {
        logger.info("Non-retryable activity: {}", af.getType());
      }
      throw e;
    }
    // be sure to check that all handlers have been completed before exit
    Workflow.await(Workflow::isEveryHandlerFinished);
  }

  // startSyncToStorage
  // Kick off the application state sync asynchronously.
  // This is akin to dirty checking inside an OR/M IdentityMap implementation
  // Just mark the workflow as `shouldSync` and the condition unblocks to perform the work.
  private CancellationScope startSyncToStorage() {
    CancellationScope syncScope =
        Workflow.newCancellationScope(
            () -> {
              while (true) {
                try {
                  // calling this BEFORE the condition effectively dumps our state to
                  // storage before waiting to be told to resync
                  integrationsHandlers.syncToStorage(new SyncToStorageRequest(this.state));
                  shouldSync = false;
                } catch (ActivityFailure e) {
                  // maybe we can count the number of times we are willing to fail to report
                  // that our Workflow state is out of sync now
                  logger.warn("Sync failed", e);
                }
                Workflow.await(() -> shouldSync);
              }
            });
    return syncScope;
  }

  @Override
  public EntityOnboardingState getState() {
    return state;
  }

  @Override
  public void approve(ApproveEntityRequest cmd) {
    // No need for an idempotency key here since we are not concerned about
    // duplicating messages, though we are _are_ using ContinueAsNew.
    state =
        new EntityOnboardingState(
            state.id(), state.currentValue(), new Approval(ApprovalStatus.APPROVED, cmd.comment()));
  }

  @Override
  public void reject(RejectEntityRequest cmd) {
    // No need for an idempotency key here since we are not concerned about
    // duplicating messages, though we are _are_ using ContinueAsNew.
    state =
        new EntityOnboardingState(
            state.id(), state.currentValue(), new Approval(ApprovalStatus.REJECTED, cmd.comment()));
  }

  @Override
  public void forceSync() {
    shouldSync = true;
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
