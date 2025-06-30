package io.temporal.fsi.domain.applications.workflows.userRegistrations;

import io.temporal.fsi.api.applications.v1.GetUserRegistrationsResponse;
import io.temporal.fsi.api.applications.v1.RegisterUserRequest;
import io.temporal.fsi.api.applications.v1.RegisterUserResponse;
import io.temporal.fsi.api.applications.v1.StartUserRegistrationsRequest;
import io.temporal.workflow.*;

@WorkflowInterface
public interface UserRegistrations {

  @WorkflowMethod
  void start(StartUserRegistrationsRequest args);

  @UpdateMethod
  RegisterUserResponse registerUser(RegisterUserRequest cmd);

  @QueryMethod
  GetUserRegistrationsResponse getState();
}
