package io.temporal.app.domain.products;

import io.temporal.app.domain.messages.commands.FulfillAccommodationRequest;
import io.temporal.app.domain.messages.commands.FulfillFlightRequest;
import io.temporal.app.domain.messages.commands.FulfillTaxiRequest;
import io.temporal.app.domain.messages.queries.GetProductFulfillmentConfigurationRequest;
import io.temporal.app.domain.messages.queries.GetProductFulfillmentConfigurationResponse;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Component;

@Component("product-handlers")
public class HandlersImpl implements ProductHandlers {

  @Override
  public void fulfillFlight(FulfillFlightRequest cmd) {}

  @Override
  public void fulfillAccommodation(FulfillAccommodationRequest cmd) {}

  @Override
  public void fulfillTaxi(FulfillTaxiRequest cmd) {}

  @Override
  public GetProductFulfillmentConfigurationResponse getFulfillmentConfiguration(
      GetProductFulfillmentConfigurationRequest cmd) {
    throw new NotImplementedException();
    //    return null;
  }
}
