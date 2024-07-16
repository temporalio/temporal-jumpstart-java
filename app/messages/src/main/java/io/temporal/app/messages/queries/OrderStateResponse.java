package io.temporal.app.messages.queries;

import io.temporal.app.messages.commands.CompleteProductFulfillmentRequest;
import io.temporal.app.messages.commands.ProductFulfillmentRequest;
import io.temporal.app.messages.orchestrations.SubmitOrderRequest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class OrderStateResponse {

  private final List<CompleteProductFulfillmentRequest> completions = new ArrayList<>() {};
  private SubmitOrderRequest submitOrderRequest;
  private boolean partiallyFulfilled;
  private LinkedHashMap<String, ProductFulfillmentRequest> requestsSucceeded =
      new LinkedHashMap<>();
  private LinkedHashMap<String, ProductFulfillmentRequest> requestsFailed = new LinkedHashMap<>();
  private boolean fulfillmentsTimedout;

  public OrderStateResponse() {}

  public OrderStateResponse(SubmitOrderRequest args) {
    this.submitOrderRequest = args;
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
    this.partiallyFulfilled = isPartiallyFulfilled;
  }

  public boolean isPartiallyFulfilled() {
    return partiallyFulfilled;
  }

  public LinkedHashMap<String, ProductFulfillmentRequest> getRequestsSucceeded() {
    return requestsSucceeded;
  }

  public void setRequestsSucceeded(
      LinkedHashMap<String, ProductFulfillmentRequest> requestsSucceeded) {
    this.requestsSucceeded = requestsSucceeded;
  }

  public LinkedHashMap<String, ProductFulfillmentRequest> getRequestsFailed() {
    return requestsFailed;
  }

  public void setRequestsFailed(LinkedHashMap<String, ProductFulfillmentRequest> requestsFailed) {
    this.requestsFailed = requestsFailed;
  }

  public boolean isFulfillmentsTimedout() {
    return fulfillmentsTimedout;
  }

  public void setFulfillmentsTimedout(boolean fulfillmentsTimedout) {
    this.fulfillmentsTimedout = fulfillmentsTimedout;
  }
}
