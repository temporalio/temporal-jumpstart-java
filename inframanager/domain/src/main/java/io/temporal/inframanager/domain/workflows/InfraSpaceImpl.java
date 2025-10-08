package io.temporal.inframanager.domain.workflows;

import io.temporal.inframanager.messages.jumpstart.domain.inframanager.workflows.v1.GetInfraSpaceStateResponse;
import io.temporal.inframanager.messages.jumpstart.domain.inframanager.workflows.v1.StartInfraSpaceRequest;
import io.temporal.workflow.WorkflowInit;

public class InfraSpaceImpl implements InfraSpace {

  private final GetInfraSpaceStateResponse state;

  @WorkflowInit
  public InfraSpaceImpl(StartInfraSpaceRequest args) {
    this.state =
        GetInfraSpaceStateResponse.newBuilder().setArgs(args).setName(args.getName()).build();
  }

  @Override
  public void execute(StartInfraSpaceRequest args) {
    //    throw ApplicationFailure.newFailure("Invalid", Errors.INVALID_ARGS.name());
    //        throw new RuntimeException("Not implemented");
  }

  @Override
  public GetInfraSpaceStateResponse getState() {
    return this.state;
  }
}
