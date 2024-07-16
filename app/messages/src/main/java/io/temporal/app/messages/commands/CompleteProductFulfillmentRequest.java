package io.temporal.app.messages.commands;

import io.temporal.app.messages.values.FulfillmentStatus;
import io.temporal.app.messages.values.ProductType;

public record CompleteProductFulfillmentRequest(
    ProductType productType, String id, FulfillmentStatus fulfillmentStatus) {}
