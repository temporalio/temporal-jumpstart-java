package io.temporal.app.domain.messages.values;

public record ProductFulfillmentConfiguration(ProductType productType, int timeoutSeconds) {}
