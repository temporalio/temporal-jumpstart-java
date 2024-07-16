package io.temporal.app.domain.orchestrations;

import static java.util.Map.entry;

import io.temporal.activity.ActivityCancellationType;
import io.temporal.activity.ActivityOptions;
import io.temporal.activity.LocalActivityOptions;
import io.temporal.app.domain.products.ProductHandlers;
import io.temporal.app.messages.commands.*;
import io.temporal.app.messages.orchestrations.SubmitOrderRequest;
import io.temporal.app.messages.queries.GetProductFulfillmentConfigurationRequest;
import io.temporal.app.messages.queries.OrderStateResponse;
import io.temporal.app.messages.values.FulfillmentStatus;
import io.temporal.app.messages.values.ProductFulfillmentConfiguration;
import io.temporal.app.messages.values.ProductType;
import io.temporal.failure.ApplicationFailure;
import io.temporal.workflow.*;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderImpl implements Order {
  private final Logger logger = LoggerFactory.getLogger(OrderImpl.class);
  private final ProductHandlers configHandlers =
      Workflow.newLocalActivityStub(
          ProductHandlers.class,
          LocalActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(10)).build());

  // state
  private OrderStateResponse state;

  @Override
  public void execute(SubmitOrderRequest args) {
    state = new OrderStateResponse(args);
    if (args.fulfillmentRequests().length < 1) {
      throw ApplicationFailure.newFailure(
          "At least one product is required", Errors.INVALID_ARGS.name());
    }

    // First get config and calculate max duration we will wait to collect
    // all our 'complete' commands from the /done endpoint (via Signal).
    var productTypes =
        Arrays.stream(args.fulfillmentRequests())
            .map(ProductFulfillmentRequest::productType)
            .toList();
    var configRequest =
        new GetProductFulfillmentConfigurationRequest(productTypes.toArray(new ProductType[0]));
    var config = configHandlers.getFulfillmentConfiguration(configRequest);
    var maxTimeoutSecs =
        config.config().stream()
            .mapToInt(ProductFulfillmentConfiguration::timeoutSeconds)
            .max()
            .orElseThrow(NoSuchElementException::new);

    if (maxTimeoutSecs <= 0) {
      throw ApplicationFailure.newFailure(
          MessageFormat.format("Invalid timeout {0}", maxTimeoutSecs), Errors.BAD_CONFIG.name());
    }

    CancellationScope cancellationScope =
        Workflow.newCancellationScope(
            () -> {
              startFulfillments(args, maxTimeoutSecs);
            });

    // Kick off the fulfillment requests asynchronously.
    cancellationScope.run();
    logger.info("Order waiting {} seconds for completion", maxTimeoutSecs);

    // Now wait until: either all the completions are Signaled, or we timeout.
    // If all the fulfillment requests could not get started we want to cancel all the activities.
    var conditionMet =
        Workflow.await(
            Duration.ofSeconds(maxTimeoutSecs),
            () -> state.getCompletions().size() == args.fulfillmentRequests().length);
    if (!conditionMet) {
      this.state.setFulfillmentsTimedout(true);
      logger.info(
          "Some fulfillments have not completed yet: {} were completed",
          state.getCompletions().size());
      // Since we timed out, cancel any requests to products that have "hung"
      // this is slightly redundant since we are using ScheduleToClose timeout
      // but this shows how you can cancel activities that might be in flight
      cancellationScope.cancel();
    }
    state.setPartiallyFulfilled(isPartiallyFulfilled(args));
  }

  private void startFulfillments(SubmitOrderRequest args, int maxTimeoutSecs) {
    // showing how important it is to compare a single execution to the overall time we are
    // willing to wait for each activity to complete
    var stc = Math.max(maxTimeoutSecs - 3, 0);
    final ProductHandlers fulfillmentHandlers =
        Workflow.newActivityStub(
            ProductHandlers.class,
            ActivityOptions.newBuilder()
                // we just want to shut down these activities upon cancellation, no request needed
                // for our activities to pick up
                // since they are supposed to be quick and will not implement heartbeating
                .setCancellationType(ActivityCancellationType.ABANDON)
                .setScheduleToCloseTimeout(Duration.ofSeconds(maxTimeoutSecs))
                .build());
    final Map<ProductType, Function<ProductFulfillmentRequest, Promise<Void>>>
        productFulfillmentFuncs =
            Map.ofEntries(
                entry(
                    ProductType.ACCOMMODATION,
                    (r) ->
                        wrap(
                            (FulfillAccommodationRequest) r,
                            fulfillmentHandlers::fulfillAccommodation)),
                entry(
                    ProductType.TAXI,
                    (r) -> wrap((FulfillTaxiRequest) r, fulfillmentHandlers::fulfillTaxi)),
                entry(
                    ProductType.FLIGHT,
                    (r) -> wrap((FulfillFlightRequest) r, fulfillmentHandlers::fulfillFlight)));
    for (var item : args.fulfillmentRequests()) {
      var func = productFulfillmentFuncs.get(item.productType());
      if (func == null) {
        logger.warn("{} is not a valid fulfillment request", item.productType());
      } else {
        func.apply(item);
      }
    }
  }

  // Order isPartiallyFulfilled if either all the requests were made but not all of
  // them were completed OR
  // some of them were returned as FAILED
  private boolean isPartiallyFulfilled(SubmitOrderRequest args) {
    return !state.getRequestsFailed().isEmpty()
        || state.getCompletions().size() < args.fulfillmentRequests().length
        || (state.getCompletions().stream()
            .anyMatch(r -> r.fulfillmentStatus() == FulfillmentStatus.FAILED));
  }

  @Override
  public OrderStateResponse getOrderState() {
    return state;
  }

  @Override
  public void completeProductFulfillment(CompleteProductFulfillmentRequest cmd) {
    logger.info("Completing product fulfillment {}", cmd.productType());
    if (!state.isFulfillmentsTimedout()) {
      // we can capture late arrivals of completions but what should happen to these?
      state.getCompletions().add(cmd);
    }
  }

  // wrap is a helper function to accumulate errors from activities and wrap each activity func in
  // an Async.procedure
  private <R extends ProductFulfillmentRequest> Promise<Void> wrap(
      R req, Functions.Proc1<R> fulfillRequestFunc) {
    return Async.procedure(fulfillRequestFunc, req)
        .handle(
            (v, e) -> {
              if (e == null) {
                state.getRequestsSucceeded().put(req.id(), req);
              } else {
                state.getRequestsFailed().put(req.id(), req);
              }
              return null;
            });
  }
}
