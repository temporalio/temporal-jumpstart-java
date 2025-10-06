package io.temporal.inframanager.domain.workflows;

import io.temporal.inframanager.domain.io.temporal.inframanager.domain.messages.Workflows;

public class InfraSpaceImpl implements InfraSpace {
  @Override
  public void execute(Workflows.StartInfraSpaceRequest startInfraSpaceRequest) {
    //    throw ApplicationFailure.newFailure("Invalid", Errors.INVALID_ARGS.name());
    throw new RuntimeException("Not implemented");
  }
}
