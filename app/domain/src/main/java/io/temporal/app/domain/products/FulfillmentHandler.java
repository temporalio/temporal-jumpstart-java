package io.temporal.app.domain.products;

import io.temporal.app.domain.messages.commands.ProductFulfillmentRequest;
import java.util.function.Function;

public interface FulfillmentHandler extends Function<ProductFulfillmentRequest, Void> {}
