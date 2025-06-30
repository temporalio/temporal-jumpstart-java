package io.temporal.fsi.domain.applications.workflows.userRegistrations;

import io.temporal.activity.Activity;
import io.temporal.client.WorkflowClient;
import io.temporal.failure.ApplicationFailure;
import io.temporal.fsi.api.applications.v1.RegisterUserRequest;
import io.temporal.fsi.api.applications.v1.RegisterUserResponse;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component(value = "user-registrations-activities")
public class ActivitiesImpl implements Activities {
  WorkflowClient workflowClient;

  public ActivitiesImpl(WorkflowClient workflowClient) {
    this.workflowClient = workflowClient;
  }

  @Override
  public RegisterUserResponse registerUser(RegisterUserRequest cmd) {
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      throw ApplicationFailure.newNonRetryableFailureWithCause(
          "cannot complete", "register_failed", e);
    }
    var stub =
        workflowClient.newWorkflowStub(
            UserRegistrations.class, Activity.getExecutionContext().getInfo().getWorkflowId());
    return RegisterUserResponse.newBuilder()
        .setEmail(cmd.getEmail())
        .setToken(UUID.randomUUID().toString())
        .build();
  }
}
