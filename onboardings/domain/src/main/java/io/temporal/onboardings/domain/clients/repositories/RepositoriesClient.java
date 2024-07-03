package io.temporal.onboardings.domain.clients.repositories;

import io.temporal.onboardings.domain.clients.repositories.models.Customer;
import io.temporal.onboardings.domain.clients.repositories.models.Email;
import java.util.Map;

public interface RepositoriesClient {
  void saveCrmCustomer(String id, String value);

  Customer getCrmCustomer(String id);

  Map<String, String> listCrmCustomers();

  void saveEmail(String email, String body);

  Email getEmail(String email);

  Map<String, String> listSentEmails();
}
