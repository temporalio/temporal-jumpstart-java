package io.temporal.app.api.controllers;

import io.temporal.api.enums.v1.WorkflowIdReusePolicy;
import io.temporal.app.api.messages.MyResourceGet;
import io.temporal.app.api.messages.MyResourcePut;
import io.temporal.app.domain.messages.orchestrations.SubmitOrderRequest;
import io.temporal.app.domain.orchestrations.Order;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowExecutionAlreadyStarted;
import io.temporal.client.WorkflowNotFoundException;
import io.temporal.client.WorkflowOptions;
import java.net.URI;
import java.util.ArrayList;

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
@RequestMapping("/api/orders")
public class OrdersController {

  Logger logger = LoggerFactory.getLogger(OrdersController.class);
  @Autowired WorkflowClient temporalClient;

  @Value("${spring.curriculum.task-queue}")
  String taskQueue;

  @GetMapping("/{id}")
  public ResponseEntity<MyResourceGet> onboardingGet(@PathVariable("id") String id) {
    try {
      var workflowStub = temporalClient.newWorkflowStub(Order.class, id);
      // implement this
      //            var state = workflowStub.getState();
      return new ResponseEntity<>(new MyResourceGet("do", "something"), HttpStatus.OK);
    } catch (WorkflowNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @PutMapping(
      value = "/{id}",
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  ResponseEntity<String> onboardingPut(@PathVariable String id, @RequestBody MyResourcePut params) {

    return startWorkflow(id, params);
  }

  private ResponseEntity<String> startWorkflow(String id, MyResourcePut params) {
    final WorkflowOptions options =
        WorkflowOptions.newBuilder()
            .setTaskQueue(taskQueue)
            .setWorkflowId(id)
            .setRetryOptions(null)
            .setWorkflowIdReusePolicy(
                WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_REJECT_DUPLICATE)
            .build();
    var workflowStub = temporalClient.newWorkflowStub(Order.class, options);

    var wfArgs = new SubmitOrderRequest(params.id(), params.userId(), new ArrayList<>());
    // Start the workflow execution.
    try {
      var run = WorkflowClient.start(workflowStub::execute, wfArgs);
      var headers = new HttpHeaders();
      headers.setLocation(URI.create(String.format("/api/resources/%s", id)));
      return new ResponseEntity<>(HttpStatus.ACCEPTED);
    } catch (WorkflowExecutionAlreadyStarted was) {
      logger.info("Workflow execution already started: {}", id);
      return new ResponseEntity<>(HttpStatus.ACCEPTED);
    } catch (Exception e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
