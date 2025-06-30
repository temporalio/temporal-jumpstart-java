package io.temporal.fsi.domain.accounts.workflows.wealthmanagement;

import io.temporal.activity.ActivityOptions;
import io.temporal.failure.ApplicationFailure;
import io.temporal.fsi.api.applications.v1.*;
import io.temporal.workflow.UpdateValidatorMethod;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInit;
import java.time.Duration;

public class AccountImpl implements Account {
  private GetWealthManagementAccountResponse state;
  private final Activities acts;

  @WorkflowInit
  public AccountImpl(OpenWealthManagementAccountRequest cmd) {
    state = GetWealthManagementAccountResponse.getDefaultInstance();
    acts =
        Workflow.newActivityStub(
            Activities.class,
            ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(15)).build());
  }

  @Override
  public void start(OpenWealthManagementAccountRequest args) {
    acts.validateEligibility(
        ValidateEligibleRequest.newBuilder().setUserId(args.getUserId()).build());
    Workflow.await(() -> state.getIsCompleted() || state.hasMatchClient());

    if (state.getIsCompleted()) {
      // early return since we are done
      return;
    }
    // our match service reported the client was matched
    // so request the `link` from some service
    if (state.hasMatchClient() && !state.getMatchClient().getNotFound()) {
      acts.requestExistingClient(
          RequestExistingClientRequest.newBuilder()
              .setBirthdate(state.getBirthdate())
              .setSsn(state.getSsn())
              .setName(state.getName())
              .build());
      // here we are blocked until someone passes in the existing client details
      // we can time this out if we want to...
      Workflow.await(() -> !state.getClientId().isEmpty());
    }
  }

  @Override
  public GetWealthManagementAccountResponse getState() {
    return state;
  }

  @UpdateValidatorMethod(updateName = "matchClient")
  void validateMatchClient(MatchClientRequest cmd) {
    if (state.getClientId().isEmpty()) {
      throw ApplicationFailure.newFailure("client_id already exists", "client_id_exists");
    }
  }

  @Override
  public GetWealthManagementAccountResponse matchClient(MatchClientRequest cmd) {
    var match = acts.matchClient(cmd);
    state =
        state.toBuilder()
            .setName(cmd.getName())
            .setMatchClient(match)
            .setSsn(cmd.getSsn())
            .setBirthdate(cmd.getBirthdate())
            .setClientId(match.getClientId().isEmpty() ? "" : match.getClientId())
            .build();

    return state;
  }

  @Override
  public void linkExistingClient(LinkExistingClientRequest cmd) {
    state = state.toBuilder().setClientId(cmd.getClientId()).build();
  }
}
