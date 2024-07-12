package io.temporal.app.messages.queries;


import io.temporal.app.messages.commands.CompleteProductFulfillmentRequest;
import io.temporal.app.messages.orchestrations.SubmitOrderRequest;

import java.util.ArrayList;
import java.util.List;

public class OrderStateResponse {

  private final List<Exception> fulfillmentFailures = new ArrayList<>();
  private final List<CompleteProductFulfillmentRequest> completions = new ArrayList<>() {};
  private SubmitOrderRequest submitOrderRequest;
  private boolean allFulfillmentsStarted;
  private boolean isPartiallyFulfilled;

  public OrderStateResponse() {}

  public OrderStateResponse(SubmitOrderRequest args) {
    this.submitOrderRequest = args;
  }

  public List<Exception> getFulfillmentFailures() {
    return fulfillmentFailures;
  }

  public boolean isAllFulfillmentsStarted() {
    return allFulfillmentsStarted;
  }

  public void setAllFulfillmentsStarted(boolean allFulfillmentsStarted) {
    this.allFulfillmentsStarted = allFulfillmentsStarted;
  }

  public List<CompleteProductFulfillmentRequest> getCompletions() {
    return completions;
  }

  public SubmitOrderRequest getSubmitOrderRequest() {
    return submitOrderRequest;
  }

  public void setSubmitOrderRequest(SubmitOrderRequest submitOrderRequest) {
    this.submitOrderRequest = submitOrderRequest;
  }

  public void setPartiallyFulfilled(boolean isPartiallyFulfilled) {
    this.isPartiallyFulfilled = isPartiallyFulfilled;
  }

  public boolean isPartiallyFulfilled() {
    return isPartiallyFulfilled;
  }
}
