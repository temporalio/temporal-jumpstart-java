package io.temporal.onboardings.domain.clients.crm;

import io.temporal.onboardings.domain.clients.repositories.RepositoriesClient;
import java.net.ConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

@Component
public class InMemoryCrmClient implements CrmClient {

  Logger logger = LoggerFactory.getLogger(InMemoryCrmClient.class);
  RepositoriesClient repositoriesClient;

  public InMemoryCrmClient(RepositoriesClient repositoriesClient) {
    this.repositoriesClient = repositoriesClient;
  }

  @Override
  public void registerCustomer(String id, String value) throws ConnectException {

    if (value.contains("timeout")) {
      throw new ConnectException("<Spoof>: Failed to connect to API");
    }
    var c = repositoriesClient.getCrmCustomer(id);
    if (c == null) {
      repositoriesClient.saveCrmCustomer(id, value);
    }
    logger.info("register customer with id {}/{}", id, value);
  }

  @Override
  public String getCustomerById(String id) {
    var value = repositoriesClient.getCrmCustomer(id);
    if (value == null) {
      throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
    }
    return value.value();
  }
}
