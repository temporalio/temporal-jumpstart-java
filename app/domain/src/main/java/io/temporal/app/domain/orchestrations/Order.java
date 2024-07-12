package io.temporal.app.domain.orchestrations;

import io.temporal.app.messages.commands.CompleteProductFulfillmentRequest;
import io.temporal.app.messages.orchestrations.SubmitOrderRequest;
import io.temporal.app.messages.queries.OrderStateResponse;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface Order {
  @WorkflowMethod
  void execute(SubmitOrderRequest args);

  @QueryMethod
  OrderStateResponse getOrderState();

  @SignalMethod
  void completeProductFulfillment(CompleteProductFulfillmentRequest cmd);
}
