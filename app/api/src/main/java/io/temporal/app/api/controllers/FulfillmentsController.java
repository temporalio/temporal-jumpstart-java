package io.temporal.app.api.controllers;

import io.temporal.app.domain.orchestrations.Order;
import io.temporal.app.messages.api.FulfillmentPut;
import io.temporal.app.messages.api.OrderGet;
import io.temporal.app.messages.commands.CompleteProductFulfillmentRequest;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowNotFoundException;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fulfillments")
public class FulfillmentsController {

  Logger logger = LoggerFactory.getLogger(FulfillmentsController.class);
  @Autowired WorkflowClient temporalClient;

  @GetMapping("/{id}")
  public ResponseEntity<OrderGet> fulfillmentGet(@PathVariable("id") String id) {
    throw new NotImplementedException();
  }

  @PutMapping(
      value = "/{id}",
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  ResponseEntity<String> fulfillmentPut(
      @PathVariable String id, @RequestBody FulfillmentPut params) {

    logger.info("received PUT request for id {}", id);
    try {
      var wfStub = temporalClient.newWorkflowStub(Order.class, params.orderId());
      wfStub.completeProductFulfillment(
          new CompleteProductFulfillmentRequest(
              params.productType(), id, params.fulfillmentStatus()));
      return new ResponseEntity<>(HttpStatus.ACCEPTED);
    } catch (WorkflowNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }
}
