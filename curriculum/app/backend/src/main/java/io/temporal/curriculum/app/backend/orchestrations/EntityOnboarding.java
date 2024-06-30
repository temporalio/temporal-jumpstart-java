package io.temporal.curriculum.app.backend.orchestrations;

import io.temporal.curriculum.app.backend.messages.orchestrations.OnboardEntityRequest;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface EntityOnboarding {
  @WorkflowMethod
  void execute(OnboardEntityRequest args);
}
