package io.temporal.app.domain.orchestrations;

import static java.util.Map.entry;

import io.temporal.activity.ActivityOptions;
import io.temporal.activity.LocalActivityOptions;
import io.temporal.app.domain.messages.commands.*;
import io.temporal.app.domain.messages.orchestrations.SubmitOrderRequest;
import io.temporal.app.domain.messages.queries.GetProductFulfillmentConfigurationRequest;
import io.temporal.app.domain.messages.queries.OrderStateResponse;
import io.temporal.app.domain.messages.values.FulfillmentStatus;
import io.temporal.app.domain.messages.values.ProductFulfillmentConfiguration;
import io.temporal.app.domain.messages.values.ProductType;
import io.temporal.app.domain.products.ProductHandlers;
import io.temporal.failure.ApplicationFailure;
import io.temporal.workflow.*;
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
  private final ProductHandlers fulfillmentHandlers =
      Workflow.newActivityStub(
          ProductHandlers.class,
          ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(30)).build());
  private final Map<ProductType, Function<ProductFulfillmentRequest, Promise<Void>>>
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

    CancellationScope cancellationScope =
        Workflow.newCancellationScope(
            () -> {
              var results = new ArrayList<Promise<Void>>();

              for (var item : args.fulfillmentRequests()) {
                var func = productFulfillmentFuncs.get(item.productType());
                if (func == null) {
                  logger.warn("{} is not a valid fulfillment request", item.productType());
                } else {
                  results.add(func.apply(item));
                }
              }
              Promise.allOf(results).get();
              state.setAllFulfillmentsStarted(true);
            });

    // Kick off the fulfillment requests asynchronously.
    cancellationScope.run();

    // Now wait until: either all the completions are Signaled, or we timeout.
    // If all the fulfillment requests could not get started we want to cancel all the activities.
    var conditionMet =
        Workflow.await(
            Duration.ofSeconds(maxTimeoutSecs),
            () ->
                state.isAllFulfillmentsStarted()
                    && state.getCompletions().size() == args.fulfillmentRequests().length);
    if (!conditionMet) {
      // Since we timed out, cancel any requests to products that have "hung"
      cancellationScope.cancel();
      if(!state.isAllFulfillmentsStarted()) {
        throw ApplicationFailure.newFailure("Some of the product fulfillment requests could not be made. Check orderState for details", Errors.HUNG_PRODUCT_FULFILLMENT_REQUESTS.name());
      }
    }
    // mark our Order as partially fulfilled if either all the requests were made but not all of
    // them were completed OR
    // some of them were returned as FAILED
    state.setPartiallyFulfilled(
        (state.isAllFulfillmentsStarted()
                && state.getCompletions().size() < args.fulfillmentRequests().length)
            || (state.getCompletions().stream()
                .anyMatch(r -> r.fulfillmentStatus() == FulfillmentStatus.FAILED)));
  }

  @Override
  public OrderStateResponse getOrderState() {
    return state;
  }

  @Override
  public void completeProductFulfillment(CompleteProductFulfillmentRequest cmd) {
    logger.info("Completing product fulfillment {}", cmd.productType());
    state.getCompletions().add(cmd);
  }

  // wrap is a helper function to accumulate errors from activities and wrap each activity func in
  // an Async.procedure
  private <R> Promise<Void> wrap(R req, Functions.Proc1<R> fulfillRequestFunc) {
    return Async.procedure(fulfillRequestFunc, req)
        .handle(
            (v, e) -> {
              if (e != null) {
                state.getFulfillmentFailures().add(e);
              }
              return null;
            });
  }
}
