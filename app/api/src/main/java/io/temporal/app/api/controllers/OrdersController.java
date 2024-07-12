package io.temporal.app.api.controllers;

import io.temporal.api.enums.v1.WorkflowIdReusePolicy;
import io.temporal.app.messages.api.*;
import io.temporal.app.messages.commands.FulfillAccommodationRequest;
import io.temporal.app.messages.commands.FulfillTaxiRequest;
import io.temporal.app.messages.orchestrations.SubmitOrderRequest;
import io.temporal.app.messages.values.ProductType;
import io.temporal.app.domain.orchestrations.Order;
import io.temporal.app.messages.commands.FulfillFlightRequest;
import io.temporal.app.messages.commands.ProductFulfillmentRequest;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowExecutionAlreadyStarted;
import io.temporal.client.WorkflowNotFoundException;
import io.temporal.client.WorkflowOptions;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

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
  public ResponseEntity<OrderGet> onboardingGet(@PathVariable("id") String id) {
    try {
      var workflowStub = temporalClient.newWorkflowStub(Order.class, id);
      // implement this
      //            var state = workflowStub.getState();
      return new ResponseEntity<>(new OrderGet("do", "something"), HttpStatus.OK);
    } catch (WorkflowNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @PutMapping(
      value = "/{id}",
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  ResponseEntity<String> onboardingPut(@PathVariable String id, @RequestBody OrderPut params) {

    return startWorkflow(id, params);
  }

  private ResponseEntity<String> startWorkflow(String id, OrderPut params) {
    final WorkflowOptions options =
        WorkflowOptions.newBuilder()
            .setTaskQueue(taskQueue)
            .setWorkflowId(id)
            .setRetryOptions(null)
            .setWorkflowIdReusePolicy(
                WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_REJECT_DUPLICATE)
            .build();
    var workflowStub = temporalClient.newWorkflowStub(Order.class, options);
    List<ProductFulfillmentRequest> fulfillments = new ArrayList<ProductFulfillmentRequest>();
    for (FlightView view: params.flights()){
      fulfillments.add(new FulfillFlightRequest(ProductType.FLIGHT, view.id(), view.airline(), view.flightNumber()));
    }
    for (TaxiView view: params.taxis()){
      fulfillments.add(new FulfillTaxiRequest(ProductType.TAXI, view.id(), view.name(), view.pickupDateTime()));
    }
    for (AccommodationView view: params.accommodations()){
      fulfillments.add(new FulfillAccommodationRequest(ProductType.ACCOMMODATION, view.id(), view.name(),view.startDate(), view.endDate()));
    }


    var wfArgs = new SubmitOrderRequest(params.id(), params.userId(), fulfillments.toArray(new ProductFulfillmentRequest[0]));

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
