package io.temporal.app.messages.queries;


import io.temporal.app.messages.values.ProductType;

public record GetProductFulfillmentConfigurationRequest(ProductType... productTypes) {}
