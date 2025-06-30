package io.temporal.fsi.domain.applications.workflows.userRegistrations;

import io.temporal.activity.ActivityOptions;
import io.temporal.fsi.api.applications.v1.*;
import io.temporal.workflow.UpdateMethod;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInit;
import java.time.Duration;
import java.util.List;

public class UserRegistrationsImpl implements UserRegistrations {
  private GetUserRegistrationsResponse state;
  private final List<RegisterUserRequest> pending;
  private final Activities acts;

  @WorkflowInit
  public UserRegistrationsImpl(StartUserRegistrationsRequest cmd) {
    state = io.temporal.fsi.api.applications.v1.GetUserRegistrationsResponse.getDefaultInstance();
    pending = new java.util.ArrayList<>();
    acts =
        Workflow.newActivityStub(
            Activities.class,
            ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(10)).build());
  }

  @Override
  public void start(StartUserRegistrationsRequest args) {

    Workflow.await(() -> false);
  }

  @UpdateMethod(name = "registerUser")
  public void ValidateRegisterUserRequest(RegisterUserRequest cmd) {
    if (pending.stream().anyMatch(r -> r.getEmail().equals(cmd.getEmail()))
        || state.getUserRegistrationsList().stream()
            .anyMatch(r -> r.getEmail().equals(cmd.getEmail()))) {
      throw new IllegalArgumentException("Email request pending");
    }
  }

  @Override
  public RegisterUserResponse registerUser(RegisterUserRequest cmd) {
    pending.add(cmd);
    RegisterUserResponse reg = acts.registerUser(cmd);

    Workflow.getLogger(UserRegistrationsImpl.class).info("Registered user {}", reg.getEmail());
    state = state.toBuilder().addUserRegistrations(reg).build();

    return reg;
  }

  @Override
  public GetUserRegistrationsResponse getState() {
    return state;
  }
}
