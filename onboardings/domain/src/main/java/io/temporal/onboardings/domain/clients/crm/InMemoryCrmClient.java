package io.temporal.onboardings.domain.clients.crm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.net.ConnectException;
import java.util.Dictionary;
import java.util.Hashtable;

@Component
public class InMemoryCrmClient implements CrmClient {
  Logger logger = LoggerFactory.getLogger(InMemoryCrmClient.class);
  Dictionary<String, String> entities = new Hashtable<>();

  @Override
  public void registerCustomer(String id, String value) throws ConnectException {
    if(value.contains("timeout")) {
      throw new ConnectException("<Spoof>: Failed to connect to API");
    }
    logger.info("register customer with id {}/{}", id, value);
  }

  @Override
  public String getCustomerById(String id) {
    var value= entities.get(id);
    if(value == null) {
      throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
    }
    return value;
  }
}
