package io.temporal.onboardings.domain.orchestrations;

import io.temporal.onboardings.domain.messages.orchestrations.OnboardEntityRequest;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface EntityOnboarding {
  @WorkflowMethod
  void execute(OnboardEntityRequest args);
}
