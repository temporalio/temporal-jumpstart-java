package io.temporal.app.domain.messages.queries;

import io.temporal.app.domain.messages.values.ProductFulfillmentConfiguration;
import java.util.List;

public record GetProductFulfillmentConfigurationResponse(
    List<ProductFulfillmentConfiguration> config) {}
