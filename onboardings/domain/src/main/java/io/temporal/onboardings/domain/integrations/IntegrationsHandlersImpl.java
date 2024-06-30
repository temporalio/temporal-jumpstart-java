package io.temporal.onboardings.domain.integrations;

import io.temporal.onboardings.domain.clients.crm.CrmClient;
import io.temporal.onboardings.domain.messages.commands.RegisterCrmEntityRequest;
import org.springframework.stereotype.Component;

@Component("integrations-handlers")
public class IntegrationsHandlersImpl implements IntegrationsHandlers {
  private final CrmClient crmClient;

  public IntegrationsHandlersImpl(CrmClient crmClient) {
    this.crmClient = crmClient;
  }

  @Override
  public void registerCrmEntity(RegisterCrmEntityRequest cmd) {
    crmClient.registerCustomer(cmd.id(), cmd.value());
  }
}
