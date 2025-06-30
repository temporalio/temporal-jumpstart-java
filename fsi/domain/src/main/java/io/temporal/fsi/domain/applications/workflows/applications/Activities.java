package io.temporal.fsi.domain.applications.workflows.applications;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.fsi.api.applications.v1.MatchClientRequest;
import io.temporal.fsi.api.applications.v1.MatchClientResponse;
import io.temporal.fsi.api.applications.v1.ValidateEligibleRequest;
import io.temporal.fsi.api.applications.v1.ValidateEligibleResponse;

@ActivityInterface
public interface Activities {
  @ActivityMethod
  ValidateEligibleResponse validateEligibility(ValidateEligibleRequest cmd);

  @ActivityMethod
  MatchClientResponse matchClient(MatchClientRequest cmd);
}
