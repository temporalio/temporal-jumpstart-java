package io.temporal.onboardings.domain.clients.crm;

public interface CrmClient {
  void registerCustomer(String id, String value);
}
