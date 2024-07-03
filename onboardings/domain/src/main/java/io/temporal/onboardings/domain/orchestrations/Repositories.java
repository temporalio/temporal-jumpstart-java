package io.temporal.onboardings.domain.orchestrations;

import io.temporal.onboardings.domain.clients.repositories.models.Customer;
import io.temporal.onboardings.domain.clients.repositories.models.Email;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface Repositories {
  @WorkflowMethod
  void start();

  @SignalMethod
  void saveCrmCustomer(Customer customer);

  @QueryMethod
  Customer getCrmCustomer(String id);

  @SignalMethod
  void saveEmail(Email email);

  @QueryMethod
  Email getEmail(String email);
}
