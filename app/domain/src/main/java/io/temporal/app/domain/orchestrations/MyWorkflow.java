package io.temporal.app.domain.orchestrations;

import io.temporal.app.domain.messages.orchestrations.StartMyWorkflowRequest;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface MyWorkflow {
  @WorkflowMethod
  public void execute(StartMyWorkflowRequest args);
}
