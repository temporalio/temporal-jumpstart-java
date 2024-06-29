package io.temporal.curriculum.app.backend.orchestrations;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface EntityOnboarding {
  @WorkflowMethod
  public void execute();
}
