package io.temporal.onboardings.domain.orchestrations;

import io.temporal.onboardings.domain.clients.repositories.models.Customer;
import io.temporal.onboardings.domain.clients.repositories.models.Email;
import io.temporal.workflow.Workflow;
import java.util.HashMap;
import java.util.Map;

public class RepositoriesImpl implements Repositories {
  Map<String, Customer> customers = new HashMap<>();
  Map<String, Email> emails = new HashMap<>();
  boolean completed = false;

  @Override
  public void start() {
    Workflow.await(() -> completed);
  }

  @Override
  public void saveCrmCustomer(Customer customer) {
    customers.put(customer.id(), customer);
  }

  @Override
  public Customer getCrmCustomer(String id) {
    return customers.get(id);
  }

  @Override
  public void saveEmail(Email email) {
    emails.put(email.email(), email);
  }

  @Override
  public Email getEmail(String email) {
    return emails.get(email);
  }
}
