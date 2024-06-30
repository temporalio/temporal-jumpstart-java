package io.temporal.onboardings.domain.orchestrations;

import io.temporal.onboardings.domain.messages.commands.ApproveEntityRequest;
import io.temporal.onboardings.domain.messages.commands.RejectEntityRequest;
import io.temporal.onboardings.domain.messages.orchestrations.OnboardEntityRequest;
import io.temporal.onboardings.domain.messages.queries.EntityOnboardingState;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface EntityOnboarding {
  @WorkflowMethod
  void execute(OnboardEntityRequest args);

  @QueryMethod
  EntityOnboardingState getState();

  @SignalMethod
  void approve(ApproveEntityRequest cmd);

  @SignalMethod
  void reject(RejectEntityRequest cmd);
}
