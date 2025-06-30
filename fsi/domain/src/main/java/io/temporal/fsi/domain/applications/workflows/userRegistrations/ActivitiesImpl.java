package io.temporal.fsi.domain.applications.workflows.userRegistrations;

import io.temporal.fsi.api.applications.v1.RegisterUserRequest;
import io.temporal.fsi.api.applications.v1.RegisterUserResponse;
import org.springframework.stereotype.Component;

@Component(value = "user-registrations-activities")
public class ActivitiesImpl implements Activities {
  @Override
  public RegisterUserResponse registerUser(RegisterUserRequest cmd) {
    throw new UnsupportedOperationException("Not yet implemented");
  }
}
