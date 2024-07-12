package io.temporal.app.messages.orchestrations;


import io.temporal.app.messages.commands.ProductFulfillmentRequest;

public record SubmitOrderRequest(
    String id, String userId, ProductFulfillmentRequest... fulfillmentRequests) {}
