package io.temporal.onboardings.api.controllers;

import io.temporal.api.common.v1.Payload;
import io.temporal.api.common.v1.Payloads;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.enums.v1.WorkflowIdReusePolicy;
import io.temporal.api.workflowservice.v1.DescribeWorkflowExecutionRequest;
import io.temporal.api.workflowservice.v1.DescribeWorkflowExecutionResponse;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowExecutionAlreadyStarted;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.common.converter.DefaultDataConverter;
import io.temporal.onboardings.api.messages.OnboardingsGet;
import io.temporal.onboardings.api.messages.OnboardingsPut;
import io.temporal.onboardings.domain.messages.orchestrations.OnboardEntityRequest;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/onboardings")
public class OnboardingsController {

  Logger logger = LoggerFactory.getLogger(OnboardingsController.class);
  @Autowired WorkflowClient temporalClient;

  @Value("${spring.curriculum.task-queue}")
  String taskQueue;

  @GetMapping("/{id}")
  public ResponseEntity<OnboardingsGet> onboardingGet(@PathVariable("id") String id) {
    var svc = this.temporalClient.getWorkflowServiceStubs();

    WorkflowExecution execution = WorkflowExecution.newBuilder().setWorkflowId(id).build();
    DescribeWorkflowExecutionResponse desc =
        svc.blockingStub()
            .describeWorkflowExecution(
                DescribeWorkflowExecutionRequest.newBuilder()
                    .setExecution(execution)
                    .setNamespace("default")
                    .build());
    var status = desc.getWorkflowExecutionInfo().getStatus();
    var history = this.temporalClient.fetchHistory(id);
    Payloads payloads =
        history.getHistory().getEvents(0).getWorkflowExecutionStartedEventAttributes().getInput();
    OnboardEntityRequest sentRequest = null;
    for (Payload payload : payloads.getPayloadsList()) {
      // using default data converter..assumes MigrateableWorkflowParams type
      // note if you use custom data converter you would need use it instead of default
      sentRequest =
          DefaultDataConverter.newDefaultInstance()
              .fromPayload(payload, OnboardEntityRequest.class, OnboardEntityRequest.class);
    }
    var get = new OnboardingsGet(sentRequest.id(), status.toString(), sentRequest);
    return ResponseEntity.ok(get);
  }

  @PutMapping(
      value = "/{id}",
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  ResponseEntity<String> onboardingPut(
      @PathVariable String id, @RequestBody OnboardingsPut params) {

    return startOnboardEntity(id, params);
  }

  private ResponseEntity<String> startOnboardEntity(String id, OnboardingsPut params) {
    final WorkflowOptions options =
        WorkflowOptions.newBuilder()
            .setTaskQueue(taskQueue)
            .setWorkflowId(id)
            .setRetryOptions(null)
            .setWorkflowIdReusePolicy(
                WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_REJECT_DUPLICATE)
            .build();
    WorkflowStub workflowStub =
        temporalClient.newUntypedWorkflowStub("WorkflowDefinitionDoesntExistYet", options);

    var wfArgs = new OnboardEntityRequest(params.id(), params.value(), 7 * 86400, null, false);
    // Start the workflow execution.
    try {
      var run = workflowStub.start(wfArgs);
      var headers = new HttpHeaders();
      headers.setLocation(URI.create(String.format("/api/onboardings/%s", id)));
      return new ResponseEntity<>(HttpStatus.ACCEPTED);
    } catch (WorkflowExecutionAlreadyStarted was) {
      logger.info("Workflow execution already started: {}", id);
      return new ResponseEntity<>(HttpStatus.ACCEPTED);
    } catch (Exception e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
