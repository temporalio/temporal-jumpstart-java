package io.temporal.app.domain.orchestrations;

import io.temporal.app.domain.messages.orchestrations.StartMyWorkflowRequest;

public class MyWorkflowImpl implements MyWorkflow {
  @Override
  public void execute(StartMyWorkflowRequest args) {}
}
