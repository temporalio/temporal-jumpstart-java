package io.temporal.onboardings.domain.orchestrations;

import io.temporal.onboardings.domain.integrations.Handlers;
import io.temporal.onboardings.domain.messages.commands.RegisterCrmEntityRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("integrations-handlers-test")
public class IntegrationsHandlers implements Handlers {
  @Autowired CrmListener listener;

  @Override
  public void registerCrmEntity(RegisterCrmEntityRequest cmd) {
    listener.registered(cmd);
    System.out.println("yes");
  }
}
