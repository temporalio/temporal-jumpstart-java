package io.temporal.onboardings.domain.orchestrations;

import io.temporal.activity.ActivityOptions;
import io.temporal.failure.ApplicationFailure;
import io.temporal.onboardings.domain.integrations.Handlers;
import io.temporal.onboardings.domain.messages.commands.RegisterCrmEntityRequest;
import io.temporal.onboardings.domain.messages.orchestrations.OnboardEntityRequest;
import io.temporal.workflow.Workflow;
import java.time.Duration;

public class EntityOnboardingImpl implements EntityOnboarding {
  Handlers integrationsHandlers =
      Workflow.newActivityStub(
          Handlers.class,
          ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build());

  @Override
  public void execute(OnboardEntityRequest args) {
    assertValidArgs(args);
  }

  private void assertValidArgs(OnboardEntityRequest args) {
    if (args.id().isEmpty() || args.value().isEmpty()) {
      throw ApplicationFailure.newFailure("id and value are required", "invalid_args");
    }
    integrationsHandlers.registerCrmEntity(new RegisterCrmEntityRequest(args.id(), args.value()));
  }
}
