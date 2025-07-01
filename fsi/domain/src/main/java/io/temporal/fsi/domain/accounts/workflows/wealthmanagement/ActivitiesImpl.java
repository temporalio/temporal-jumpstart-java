package io.temporal.fsi.domain.accounts.workflows.wealthmanagement;

import io.temporal.activity.Activity;
import io.temporal.client.WorkflowClient;
import io.temporal.failure.ApplicationFailure;
import io.temporal.fsi.api.applications.v1.*;
import io.temporal.fsi.domain.accounts.workflows.registrations.UserRegistrations;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component(value = "account-activities")
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
    var exists = cmd.getName().contains("exists");
    var apiFailure = cmd.getName().contains("api_failure");
    var clientId = "";
    if (!exists) {
      clientId = UUID.randomUUID().toString();
    }
    // faking the api outage
    if (apiFailure && Activity.getExecutionContext().getInfo().getAttempt() <= 5) {
      throw ApplicationFailure.newFailure("api_failure", "api_failure", "faking the api failure");
    }

    return MatchClientResponse.newBuilder().setClientId(clientId).setNotFound(!exists).build();
  }

  @Override
  public void requestExistingClient(RequestExistingClientRequest cmd) {

    try {
      // SPOOF sending an email to request the existing client
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    // After 5 seconds, signal back to simulate some API or producer
    // letting the workflow know the clientId
    var stub =
        workflowClient.newWorkflowStub(
            Account.class, Activity.getExecutionContext().getInfo().getWorkflowId());
    stub.linkExistingClient(
        LinkExistingClientRequest.newBuilder()
            .setClientId(UUID.randomUUID().toString())
            .setUserId(cmd.getUserId())
            .build());
  }

  @Override
  public ForwardClientResponse forwardClient(ForwardClientRequest cmd) {
    // call salesforce API here
    return ForwardClientResponse.getDefaultInstance();
  }

  @Override
  public ApplyWealthManagementVendorResponse applyWealthManagementVendor(
      ApplyWealthManagementVendorRequest cmd) {
    // return the accountId we got from the wealth management vendor API here
    // forward the application details (collected inside an Update??)
    return ApplyWealthManagementVendorResponse.newBuilder().build();
  }

  @Override
  public void nagApplicant(NagApplicantRequest cmd) {
    // send email to remind applicant they are not done yet!
  }
}
