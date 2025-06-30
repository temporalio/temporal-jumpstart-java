package io.temporal.fsi.domain.applications.workflows.userRegistrations;

import io.temporal.fsi.api.applications.v1.*;
import io.temporal.workflow.*;

@WorkflowInterface
public interface UserRegistrations {

  @WorkflowMethod
  void start(StartUserRegistrationsRequest args);

  @UpdateMethod
  RegisterUserResponse registerUser(RegisterUserRequest cmd);

  @UpdateMethod
  AuthorizeUserResponse authorizeUser(AuthorizeUserRequest cmd);

  @QueryMethod
  GetUserRegistrationsResponse getState();
}
