package io.temporal.app.domain.products;

import io.temporal.app.messages.commands.FulfillAccommodationRequest;
import io.temporal.app.messages.commands.FulfillFlightRequest;
import io.temporal.app.messages.commands.FulfillTaxiRequest;
import io.temporal.app.messages.queries.GetProductFulfillmentConfigurationRequest;
import io.temporal.app.messages.queries.GetProductFulfillmentConfigurationResponse;
import io.temporal.app.messages.values.ProductFulfillmentConfiguration;
import io.temporal.app.messages.values.ProductType;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;

import static java.util.Map.entry;

@Component("product-handlers")
public class HandlersImpl implements ProductHandlers {
  private static final Map<ProductType, ProductFulfillmentConfiguration> defaultCfg = Map.ofEntries(
          entry(ProductType.ACCOMMODATION, new ProductFulfillmentConfiguration(ProductType.ACCOMMODATION, 30)),
          entry(ProductType.TAXI, new ProductFulfillmentConfiguration(ProductType.TAXI, 40)),
          entry(ProductType.FLIGHT, new ProductFulfillmentConfiguration(ProductType.FLIGHT, 50)));
  @Override
  public void fulfillFlight(FulfillFlightRequest cmd) {

  }

  @Override
  public void fulfillAccommodation(FulfillAccommodationRequest cmd) {}

  @Override
  public void fulfillTaxi(FulfillTaxiRequest cmd) {}

  @Override
  public GetProductFulfillmentConfigurationResponse getFulfillmentConfiguration(
      GetProductFulfillmentConfigurationRequest cmd) {

    return new GetProductFulfillmentConfigurationResponse(Arrays.stream(cmd.productTypes()).map(defaultCfg::get).toList());
  }
}
