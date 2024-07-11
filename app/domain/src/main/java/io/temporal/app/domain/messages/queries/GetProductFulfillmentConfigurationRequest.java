package io.temporal.app.domain.messages.queries;

import io.temporal.app.domain.messages.values.ProductType;

public record GetProductFulfillmentConfigurationRequest(ProductType... productTypes) {}
