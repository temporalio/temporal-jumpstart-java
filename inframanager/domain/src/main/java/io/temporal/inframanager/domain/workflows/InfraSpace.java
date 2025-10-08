package io.temporal.inframanager.domain.workflows;

import io.temporal.inframanager.domain.messages.Workflows;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface InfraSpace {
  @WorkflowMethod
  void execute(Workflows.StartInfraSpaceRequest startInfraSpaceRequest);

  @QueryMethod
  Workflows.GetInfraSpaceStateResponse getState();
}
