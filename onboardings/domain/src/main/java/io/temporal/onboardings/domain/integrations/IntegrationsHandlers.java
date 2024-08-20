package io.temporal.onboardings.domain.integrations;

import io.temporal.activity.ActivityInterface;
import io.temporal.onboardings.domain.messages.commands.RegisterCrmEntityRequest;

@ActivityInterface
public interface IntegrationsHandlers {
  void registerCrmEntity(RegisterCrmEntityRequest cmd);
}
