package io.temporal.fsi.domain.applications.workflows.applications;

import io.temporal.activity.ActivityOptions;
import io.temporal.failure.ApplicationFailure;
import io.temporal.fsi.api.applications.v1.GetApplicationResponse;
import io.temporal.fsi.api.applications.v1.MatchClientRequest;
import io.temporal.fsi.api.applications.v1.StartApplicationRequest;
import io.temporal.fsi.api.applications.v1.ValidateEligibleRequest;
import io.temporal.workflow.UpdateValidatorMethod;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInit;
import java.time.Duration;

public class ApplicationImpl implements Application {
  private final GetApplicationResponse state;
  private final Activities acts;

  @WorkflowInit
  public ApplicationImpl(StartApplicationRequest cmd) {
    state = GetApplicationResponse.getDefaultInstance();
    acts =
        Workflow.newActivityStub(
            Activities.class,
            ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(15)).build());
  }

  @Override
  public void start(StartApplicationRequest args) {
    acts.validateEligibility(
        ValidateEligibleRequest.newBuilder().setUserId(args.getUserId()).build());
    Workflow.await(state.);
  }

  @Override
  public GetApplicationResponse getState() {
    return state;
  }

  @UpdateValidatorMethod(updateName = "matchClient")
  void validateMatchClient(MatchClientRequest cmd) {
    if (state.getClientId().isEmpty()) {
      throw ApplicationFailure.newFailure("client_id already exists", "client_id_exists");
    }
  }

  @Override
  public GetApplicationResponse matchClient(MatchClientRequest cmd) {
    acts.matchClient(cmd);
    return state;
  }
}
