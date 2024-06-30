package io.temporal.onboardings.domain.integrations;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface IntegrationsHandlers {
  void registerCrmEntity(
      io.temporal.onboardings.domain.messages.commands.RegisterCrmEntityRequest cmd);
}
