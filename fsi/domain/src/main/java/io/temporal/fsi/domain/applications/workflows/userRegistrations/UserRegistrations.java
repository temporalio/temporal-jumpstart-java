package io.temporal.fsi.domain.applications.workflows.userRegistrations;

import io.temporal.fsi.api.applications.v1.RegisterUserRequest;
import io.temporal.fsi.api.applications.v1.StartUserRegistrationsRequest;
import io.temporal.workflow.UpdateMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface UserRegistrations {
  @WorkflowMethod
  void start(StartUserRegistrationsRequest args);

  @UpdateMethod
  void registerUser(RegisterUserRequest cmd);
}
