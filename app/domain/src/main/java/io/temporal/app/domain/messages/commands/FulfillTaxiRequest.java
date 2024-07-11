package io.temporal.app.domain.messages.commands;

import io.temporal.app.domain.messages.values.ProductType;
import java.util.Date;

public record FulfillTaxiRequest(
    ProductType productType, String id, String name, Date pickupDateTime)
    implements ProductFulfillmentRequest {}
