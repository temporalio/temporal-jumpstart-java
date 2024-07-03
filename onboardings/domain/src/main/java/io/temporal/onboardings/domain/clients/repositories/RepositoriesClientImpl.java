package io.temporal.onboardings.domain.clients.repositories;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.enums.v1.WorkflowIdReusePolicy;
import io.temporal.api.workflowservice.v1.DescribeWorkflowExecutionRequest;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowNotFoundException;
import io.temporal.client.WorkflowOptions;
import io.temporal.onboardings.domain.clients.repositories.models.Customer;
import io.temporal.onboardings.domain.clients.repositories.models.Email;
import io.temporal.onboardings.domain.orchestrations.Repositories;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RepositoriesClientImpl implements RepositoriesClient {
  WorkflowClient workflowClient;
  private final String repositoriesId;
  private final String taskQueue;
  boolean initialized = false;

  public RepositoriesClientImpl(
      WorkflowClient workflowClient,
      @Value("${spring.curriculum.repositoriesId}") String repositoriesId,
      @Value("${spring.curriculum.task-queue}") String taskQueue) {
    this.workflowClient = workflowClient;
    this.repositoriesId = repositoriesId;
    this.taskQueue = taskQueue;
  }

  private Repositories getWorkflowStub() {
    try {
      if (!initialized) {
        var exe =
            workflowClient
                .getWorkflowServiceStubs()
                .blockingStub()
                .describeWorkflowExecution(
                    DescribeWorkflowExecutionRequest.newBuilder()
                        .setNamespace(workflowClient.getOptions().getNamespace())
                        .setExecution(WorkflowExecution.newBuilder().setWorkflowId(repositoriesId))
                        .build());
      }
      return workflowClient.newWorkflowStub(Repositories.class, repositoriesId);

    } catch (StatusRuntimeException e) {
      if (Objects.equals(e.getStatus().getCode(), Status.Code.NOT_FOUND)) {
        return startRepositoriesWorkflow();
      }
      throw e;
    } catch (WorkflowNotFoundException e) {
      return startRepositoriesWorkflow();
    }
  }

  private Repositories startRepositoriesWorkflow() {
    var wstub =
        workflowClient.newWorkflowStub(
            Repositories.class,
            WorkflowOptions.newBuilder()
                .setWorkflowId(repositoriesId)
                .setTaskQueue(taskQueue)
                .setWorkflowIdReusePolicy(
                    WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_REJECT_DUPLICATE)
                .build());
    WorkflowClient.start(wstub::start);
    initialized = true;
    return getWorkflowStub();
  }

  @Override
  public void saveCrmCustomer(String id, String value) {
    getWorkflowStub().saveCrmCustomer(new Customer(id, value));
  }

  @Override
  public Customer getCrmCustomer(String id) {
    return getWorkflowStub().getCrmCustomer(id);
  }

  @Override
  public Map<String, String> listCrmCustomers() {
    return Map.of();
  }

  @Override
  public void saveEmail(String email, String body) {
    getWorkflowStub().saveEmail(new Email(email, body));
  }

  @Override
  public Email getEmail(String email) {
    return getWorkflowStub().getEmail(email);
  }

  @Override
  public Map<String, String> listSentEmails() {
    return Map.of();
  }
}
