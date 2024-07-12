package io.temporal.app.messages.api;

import io.temporal.app.messages.values.FulfillmentStatus;
import io.temporal.app.messages.values.ProductType;

public record FulfillmentPut(String orderId, ProductType productType, FulfillmentStatus fulfillmentStatus) {
}
