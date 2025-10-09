package io.temporal.inframanager.domain.workflows.infraspace;

import io.temporal.failure.ApplicationFailure;
import io.temporal.inframanager.messages.jumpstart.domain.inframanager.values.v1.Errors;
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
    if (args.getName().isEmpty()) {
      throw ApplicationFailure.newFailure(
          "`name` is required", Errors.ERRORS_INVALID_ARGUMENTS.name());
    }
    //        throw new RuntimeException("Not implemented");
  }

  @Override
  public GetInfraSpaceStateResponse getState() {
    return this.state;
  }
}
