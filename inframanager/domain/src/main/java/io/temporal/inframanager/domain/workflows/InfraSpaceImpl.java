package io.temporal.inframanager.domain.workflows;

import io.temporal.inframanager.domain.messages.Workflows;
import io.temporal.workflow.WorkflowInit;

public class InfraSpaceImpl implements InfraSpace {

  private final Workflows.GetInfraSpaceStateResponse state;

  @WorkflowInit
  public InfraSpaceImpl(Workflows.StartInfraSpaceRequest args) {
    this.state = new Workflows.GetInfraSpaceStateResponse(args);
    this.state.setName(args.getName());
  }

  @Override
  public void execute(Workflows.StartInfraSpaceRequest args) {
    //    throw ApplicationFailure.newFailure("Invalid", Errors.INVALID_ARGS.name());
    //        throw new RuntimeException("Not implemented");
  }

  @Override
  public Workflows.GetInfraSpaceStateResponse getState() {
    return this.state;
  }
}
