package io.temporal.fsi.domain.accounts.workflows.wealthmanagement;

import io.temporal.fsi.api.applications.v1.*;
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

  @UpdateMethod
  GetWealthManagementAccountResponse completeWealthManagementApplication(
      CompleteWealthManagementApplicationRequest cmd);

  @SignalMethod
  void markWealthManagementVendorCompleted();
}
