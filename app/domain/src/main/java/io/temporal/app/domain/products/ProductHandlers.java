package io.temporal.app.domain.products;

import io.temporal.activity.ActivityInterface;
import io.temporal.app.domain.messages.commands.FulfillAccommodationRequest;
import io.temporal.app.domain.messages.commands.FulfillFlightRequest;
import io.temporal.app.domain.messages.commands.FulfillTaxiRequest;
import io.temporal.app.domain.messages.queries.GetProductFulfillmentConfigurationRequest;
import io.temporal.app.domain.messages.queries.GetProductFulfillmentConfigurationResponse;

@ActivityInterface
public interface ProductHandlers {
  // @ActivityMethod
  void fulfillFlight(FulfillFlightRequest cmd);

  // @ActivityMethod
  void fulfillAccommodation(FulfillAccommodationRequest cmd);

  // @ActivityMethod
  void fulfillTaxi(FulfillTaxiRequest cmd);

  //  @ActivityMethod
  GetProductFulfillmentConfigurationResponse getFulfillmentConfiguration(
      GetProductFulfillmentConfigurationRequest cmd);
}
