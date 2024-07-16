package io.temporal.app.messages.commands;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.temporal.app.messages.values.ProductType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = FulfillFlightRequest.class, name = "flight"),
  @JsonSubTypes.Type(value = FulfillAccommodationRequest.class, name = "accommodation"),
  @JsonSubTypes.Type(value = FulfillTaxiRequest.class, name = "taxi")
})
public interface ProductFulfillmentRequest {
  ProductType productType();

  String id();
}
