package io.temporal.fsi.domain.applications.workflows.applications;

import io.temporal.fsi.api.applications.v1.GetApplicationResponse;
import io.temporal.fsi.api.applications.v1.MatchClientRequest;
import io.temporal.fsi.api.applications.v1.StartApplicationRequest;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.UpdateMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface Application {
  @WorkflowMethod
  void start(StartApplicationRequest args);

  @QueryMethod
  GetApplicationResponse getState();

  @UpdateMethod
  GetApplicationResponse matchClient(MatchClientRequest cmd);
}
