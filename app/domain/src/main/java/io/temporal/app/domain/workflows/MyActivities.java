package io.temporal.app.domain.workflows;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface MyActivities {
  @ActivityMethod
  public void execute();
}
