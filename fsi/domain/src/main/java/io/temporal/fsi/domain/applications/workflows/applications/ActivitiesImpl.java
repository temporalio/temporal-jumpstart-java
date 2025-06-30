package io.temporal.fsi.domain.applications.workflows.applications;

import io.temporal.client.WorkflowClient;
import io.temporal.failure.ApplicationFailure;
import io.temporal.fsi.api.applications.v1.MatchClientRequest;
import io.temporal.fsi.api.applications.v1.MatchClientResponse;
import io.temporal.fsi.api.applications.v1.ValidateEligibleRequest;
import io.temporal.fsi.api.applications.v1.ValidateEligibleResponse;
import io.temporal.fsi.domain.applications.workflows.userRegistrations.UserRegistrations;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component(value = "application-activities")
public class ActivitiesImpl implements Activities {
  private WorkflowClient workflowClient;

  public ActivitiesImpl(WorkflowClient workflowClient) {
    this.workflowClient = workflowClient;
  }

  @Override
  public ValidateEligibleResponse validateEligibility(ValidateEligibleRequest cmd) {
    var users = workflowClient.newWorkflowStub(UserRegistrations.class, "users");
    var user =
        users.getState().getUsersList().stream()
            .filter(r -> Objects.equals(r.getUserId(), cmd.getUserId()))
            .findFirst();

    if (!user.isPresent()) {
      throw ApplicationFailure.newNonRetryableFailure(
          "user not found", "user_not_found", "user %s not found", cmd.getUserId());
    }
    return ValidateEligibleResponse.newBuilder().build();
  }

  @Override
  public MatchClientResponse matchClient(MatchClientRequest cmd) {
    return null;
  }
}
