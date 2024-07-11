package io.temporal.app.domain.orchestrations;

import io.temporal.workflow.WorkflowInterface;

@WorkflowInterface
public interface Repositories {
  void execute();
}
