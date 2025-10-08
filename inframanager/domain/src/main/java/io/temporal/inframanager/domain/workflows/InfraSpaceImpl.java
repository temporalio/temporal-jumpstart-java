package io.temporal.inframanager.domain.workflows;

import io.temporal.failure.ApplicationFailure;
import io.temporal.inframanager.domain.io.temporal.inframanager.domain.messages.Errors;
import io.temporal.inframanager.domain.io.temporal.inframanager.domain.messages.Workflows;
import io.temporal.workflow.Workflow;

public class InfraSpaceImpl implements InfraSpace {
  @Override
  public void execute(Workflows.StartInfraSpaceRequest args) {
    state.Exceptions.Add(new BadArgsException(args));
//    throw ApplicationFailure.newFailure("Invalid", Errors.INVALID_ARGS.name());
//        throw new RuntimeException("Not implemented");
  }
}
