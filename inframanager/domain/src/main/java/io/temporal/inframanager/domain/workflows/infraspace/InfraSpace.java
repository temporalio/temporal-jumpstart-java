package io.temporal.inframanager.domain.workflows.infraspace;

import io.temporal.inframanager.messages.jumpstart.domain.inframanager.workflows.v1.GetInfraSpaceStateResponse;
import io.temporal.inframanager.messages.jumpstart.domain.inframanager.workflows.v1.StartInfraSpaceRequest;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface InfraSpace {
  @WorkflowMethod
  void execute(StartInfraSpaceRequest startInfraSpaceRequest);

  @QueryMethod
  GetInfraSpaceStateResponse getState();
}
