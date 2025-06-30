package io.temporal.fsi.domain.accounts.workflows.wealthmanagement;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.fsi.api.applications.v1.*;

@ActivityInterface
public interface Activities {
  @ActivityMethod
  ValidateEligibleResponse validateEligibility(ValidateEligibleRequest cmd);

  @ActivityMethod
  MatchClientResponse matchClient(MatchClientRequest cmd);

  @ActivityMethod
  void requestExistingClient(RequestExistingClientRequest cmd);

  @ActivityMethod
  ForwardClientResponse forwardClient(ForwardClientRequest cmd);

  @ActivityMethod
  ApplyWealthManagementVendorResponse applyWealthManagementVendor(
      ApplyWealthManagementVendorRequest cmd);
}
