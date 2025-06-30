package io.temporal.fsi.domain.accounts.workflows.wealthmanagement;

import io.temporal.fsi.api.applications.v1.GetWealthManagementAccountResponse;
import io.temporal.fsi.api.applications.v1.LinkExistingClientRequest;
import io.temporal.fsi.api.applications.v1.MatchClientRequest;
import io.temporal.fsi.api.applications.v1.OpenWealthManagementAccountRequest;
import io.temporal.workflow.*;

@WorkflowInterface
public interface Account {
  @WorkflowMethod
  void start(OpenWealthManagementAccountRequest args);

  @QueryMethod
  GetWealthManagementAccountResponse getState();

  @UpdateMethod
  GetWealthManagementAccountResponse matchClient(MatchClientRequest cmd);

  @SignalMethod
  void linkExistingClient(LinkExistingClientRequest cmd);
}
