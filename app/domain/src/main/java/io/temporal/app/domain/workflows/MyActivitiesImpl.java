package io.temporal.app.domain.workflows;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Component;

@Component("my-activities")
public class MyActivitiesImpl implements MyActivities {
  @Override
  public void execute() {
    throw new NotImplementedException();
  }
}
