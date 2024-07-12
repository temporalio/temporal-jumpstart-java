package io.temporal.app.messages.values;

public record ProductFulfillmentConfiguration(ProductType productType, int timeoutSeconds) {}
