package io.temporal.app.domain.messages.commands;

import io.temporal.app.domain.messages.values.FulfillmentStatus;
import io.temporal.app.domain.messages.values.ProductType;

public record CompleteProductFulfillmentRequest(
    ProductType productType, String id, FulfillmentStatus fulfillmentStatus) {}
