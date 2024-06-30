package io.temporal.curriculum.app.backend.orchestrations;

import io.temporal.curriculum.app.backend.messages.orchestrations.OnboardEntityRequest;
import io.temporal.failure.ApplicationFailure;

public class EntityOnboardingImpl implements EntityOnboarding {
  @Override
  public void execute(OnboardEntityRequest args) {
    assertValidArgs(args);
  }

  private void assertValidArgs(OnboardEntityRequest args) {
    if (args.id().isEmpty() || args.value().isEmpty()) {
      throw ApplicationFailure.newFailure("id and value are required", "invalid_args");
    }
  }
}
