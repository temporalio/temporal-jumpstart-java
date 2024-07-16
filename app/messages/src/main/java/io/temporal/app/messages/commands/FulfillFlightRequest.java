package io.temporal.app.messages.commands;

import io.temporal.app.messages.values.ProductType;

public record FulfillFlightRequest(
    ProductType productType, String id, String airline, String flightNumber)
    implements ProductFulfillmentRequest {}
