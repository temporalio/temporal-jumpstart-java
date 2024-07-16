package io.temporal.app.messages.commands;

import io.temporal.app.messages.values.ProductType;
import java.util.Date;

public record FulfillTaxiRequest(
    ProductType productType, String id, String name, Date pickupDateTime)
    implements ProductFulfillmentRequest {}