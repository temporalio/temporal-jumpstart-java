package io.temporal.onboardings.domain.orchestrations;

import io.temporal.onboardings.domain.messages.commands.RegisterCrmEntityRequest;

public interface CrmListener {
  void registered(RegisterCrmEntityRequest req);
}
