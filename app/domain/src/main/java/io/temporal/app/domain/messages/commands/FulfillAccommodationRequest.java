package io.temporal.app.domain.messages.commands;

import io.temporal.app.domain.messages.values.ProductType;
import java.util.Date;

public record FulfillAccommodationRequest(
    ProductType productType, String id, String name, Date startDate, Date endDate)
    implements ProductFulfillmentRequest {}
