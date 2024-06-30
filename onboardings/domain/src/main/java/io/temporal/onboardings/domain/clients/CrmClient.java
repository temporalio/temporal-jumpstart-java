package io.temporal.onboardings.domain.clients;

public interface CrmClient {
  void registerCustomer(String id, String value);
}
