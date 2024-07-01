package io.temporal.onboardings.domain.integrations;

import io.temporal.failure.ApplicationFailure;
import io.temporal.onboardings.domain.clients.crm.CrmClient;
import io.temporal.onboardings.domain.messages.commands.RegisterCrmEntityRequest;
import io.temporal.onboardings.domain.messages.orchestrations.Errors;
import java.net.ConnectException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

@Component("integrations-handlers")
public class IntegrationsHandlersImpl implements IntegrationsHandlers {
  private final CrmClient crmClient;

  public IntegrationsHandlersImpl(CrmClient crmClient) {
    this.crmClient = crmClient;
  }

  @Override
  public void registerCrmEntity(RegisterCrmEntityRequest cmd) {
    try {
      // Idempotency check - does the Entity already exist?
      // If so, just return
      var value = crmClient.getCustomerById(cmd.id());
      return;
    } catch (HttpClientErrorException e) {
      if (!e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
        throw e;
      }
    }
    try {
      crmClient.registerCustomer(cmd.id(), cmd.value());
    } catch (ConnectException e) {
      throw ApplicationFailure.newFailureWithCause(
          "Failed to connect with CRM service.", Errors.SERVICE_UNRECOVERABLE.name(), e);
    }
  }
}
