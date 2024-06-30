package io.temporal.onboardings.domain.clients.crm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InMemoryCrmClient implements CrmClient {
  Logger logger = LoggerFactory.getLogger(InMemoryCrmClient.class);

  @Override
  public void registerCustomer(String id, String value) {
    logger.info("register customer with id {}/{}", id, value);
  }
}
