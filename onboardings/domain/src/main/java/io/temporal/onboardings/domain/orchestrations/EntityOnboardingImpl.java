package io.temporal.onboardings.domain.orchestrations;

import io.temporal.failure.ApplicationFailure;
import io.temporal.onboardings.domain.messages.orchestrations.OnboardEntityRequest;

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
