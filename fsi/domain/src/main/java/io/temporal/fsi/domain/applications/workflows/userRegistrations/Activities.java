package io.temporal.fsi.domain.applications.workflows.userRegistrations;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.fsi.api.applications.v1.AuthorizeUserRequest;
import io.temporal.fsi.api.applications.v1.AuthorizeUserResponse;
import io.temporal.fsi.api.applications.v1.RegisterUserRequest;
import io.temporal.fsi.api.applications.v1.RegisterUserResponse;

@ActivityInterface
public interface Activities {
  @ActivityMethod
  RegisterUserResponse registerUser(RegisterUserRequest cmd);

  @ActivityMethod
  AuthorizeUserResponse authorizeUser(AuthorizeUserRequest cmd);
}
