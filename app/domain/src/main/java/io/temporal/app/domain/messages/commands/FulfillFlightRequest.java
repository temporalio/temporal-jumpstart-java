package io.temporal.app.domain.messages.commands;

import io.temporal.app.domain.messages.values.ProductType;

public record FulfillFlightRequest(
    ProductType productType, String id, String airline, String flightNumber)
    implements ProductFulfillmentRequest {}
