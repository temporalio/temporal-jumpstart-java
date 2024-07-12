package io.temporal.app.messages.queries;


import io.temporal.app.messages.values.ProductFulfillmentConfiguration;

import java.util.List;

public record GetProductFulfillmentConfigurationResponse(
    List<ProductFulfillmentConfiguration> config) {}
