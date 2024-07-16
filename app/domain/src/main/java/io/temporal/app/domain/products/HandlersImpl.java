package io.temporal.app.domain.products;

import static java.util.Map.entry;

import io.temporal.activity.Activity;
import io.temporal.app.messages.api.FulfillmentPut;
import io.temporal.app.messages.commands.FulfillAccommodationRequest;
import io.temporal.app.messages.commands.FulfillFlightRequest;
import io.temporal.app.messages.commands.FulfillTaxiRequest;
import io.temporal.app.messages.queries.GetProductFulfillmentConfigurationRequest;
import io.temporal.app.messages.queries.GetProductFulfillmentConfigurationResponse;
import io.temporal.app.messages.values.FulfillmentStatus;
import io.temporal.app.messages.values.ProductFulfillmentConfiguration;
import io.temporal.app.messages.values.ProductType;
import io.temporal.failure.ApplicationFailure;
import java.net.URI;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component("product-handlers")
public class HandlersImpl implements ProductHandlers {
  private final RestClient restClient;
  private final String serverPort;

  public HandlersImpl(@Value("${server.port}") String serverPort) {
    this.serverPort = serverPort;
    restClient = RestClient.create();
  }

  private static final Map<ProductType, ProductFulfillmentConfiguration> defaultCfg =
      Map.ofEntries(
          entry(
              ProductType.ACCOMMODATION,
              new ProductFulfillmentConfiguration(ProductType.ACCOMMODATION, 30)),
          entry(ProductType.TAXI, new ProductFulfillmentConfiguration(ProductType.TAXI, 40)),
          entry(ProductType.FLIGHT, new ProductFulfillmentConfiguration(ProductType.FLIGHT, 50)));

  private void putFulfillment(String id, FulfillmentPut put) {
    var info = Activity.getExecutionContext().getInfo();
    var body = put;
    if (id.startsWith("FAILED")) {
      body = new FulfillmentPut(put.orderId(), put.productType(), FulfillmentStatus.FAILED);
    }
    var sleepSecs = defaultCfg.get(put.productType()).timeoutSeconds() - 5;
    if (id.startsWith("DELAYED")) {
      sleepSecs = sleepSecs + 10;
    }
    try {
      Thread.sleep(Duration.ofSeconds(sleepSecs));
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    var uri = MessageFormat.format("http://localhost:{0}/api/fulfillments/{1}", serverPort, id);
    var response =
        restClient
            .put()
            .uri(URI.create(uri))
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .toBodilessEntity();
    if (response.getStatusCode().is2xxSuccessful()) {
      return;
    }
    var msg =
        MessageFormat.format(
            "Received {0}: {1}/{2}", response.getStatusCode(), put.productType(), id);
    throw ApplicationFailure.newFailure(msg, "FULFILLMENT_FAILED");
  }

  @Override
  public void fulfillFlight(FulfillFlightRequest cmd) {
    var info = Activity.getExecutionContext().getInfo();
    var body =
        new FulfillmentPut(info.getWorkflowId(), ProductType.FLIGHT, FulfillmentStatus.SUCCEEDED);
    putFulfillment(cmd.id(), body);
  }

  @Override
  public void fulfillAccommodation(FulfillAccommodationRequest cmd) {
    var info = Activity.getExecutionContext().getInfo();
    var body =
        new FulfillmentPut(
            info.getWorkflowId(), ProductType.ACCOMMODATION, FulfillmentStatus.SUCCEEDED);
    putFulfillment(cmd.id(), body);
  }

  @Override
  public void fulfillTaxi(FulfillTaxiRequest cmd) {
    var info = Activity.getExecutionContext().getInfo();
    var body =
        new FulfillmentPut(info.getWorkflowId(), ProductType.TAXI, FulfillmentStatus.SUCCEEDED);
    putFulfillment(cmd.id(), body);
  }

  @Override
  public GetProductFulfillmentConfigurationResponse getFulfillmentConfiguration(
      GetProductFulfillmentConfigurationRequest cmd) {

    return new GetProductFulfillmentConfigurationResponse(
        Arrays.stream(cmd.productTypes()).map(defaultCfg::get).toList());
  }
}
