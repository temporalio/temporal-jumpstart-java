package io.temporal.fsi.domain.accounts.workflows.wealthmanagement;

import io.temporal.activity.ActivityOptions;
import io.temporal.failure.ApplicationFailure;
import io.temporal.fsi.api.applications.v1.*;
import io.temporal.workflow.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

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
    // IF match is found, then a manual process is performed to link the “new client” with their
    // existing client record. Once linked, application process continues
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
    // Done in parallel (forward client record + process with wealth mgmt account setup)
    List<Promise<Void>> promiseList = new ArrayList<>();
    promiseList.add(
        Async.procedure(
            acts::forwardClient,
            ForwardClientRequest.newBuilder().setClientId(state.getClientId()).build()));
    promiseList.add(
        Async.procedure(
            acts::applyWealthManagementVendor,
            ApplyWealthManagementVendorRequest.newBuilder().build()));

    // Invoke all activities in parallel. Wait for all to complete
    Promise.allOf(promiseList).get();

    // TODO
    // Batch process that just calls these Workflow Executions to complete them :)
    // For now, we will simply send a signal
    Workflow.await(() -> state.getIsCompleted());
    // be sure all messages are flushed
    Workflow.await(Workflow::isEveryHandlerFinished);
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

  @Override
  public void markWealthManagementVendorCompleted() {
    state = state.toBuilder().setIsCompleted(true).build();
  }
}
