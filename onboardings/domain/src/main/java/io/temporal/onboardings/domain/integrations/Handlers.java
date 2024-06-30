package io.temporal.onboardings.domain.integrations;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface Handlers {
  void registerCrmEntity(
      io.temporal.onboardings.domain.messages.commands.RegisterCrmEntityRequest cmd);
}
