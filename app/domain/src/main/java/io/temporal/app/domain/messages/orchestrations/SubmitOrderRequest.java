package io.temporal.app.domain.messages.orchestrations;

import io.temporal.app.domain.messages.commands.ProductFulfillmentRequest;

public record SubmitOrderRequest(
    String id, String userId, ProductFulfillmentRequest... fulfillmentRequests) {}
