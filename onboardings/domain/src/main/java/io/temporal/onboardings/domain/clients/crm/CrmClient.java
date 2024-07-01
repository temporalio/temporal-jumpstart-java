package io.temporal.onboardings.domain.clients.crm;

import java.net.ConnectException;

public interface CrmClient {
  void registerCustomer(String id, String value) throws ConnectException;
  String getCustomerById(String id);
}
