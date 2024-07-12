package io.temporal.app.messages.api;

import java.util.Date;

public record TaxiView(String id, String name, Date pickupDateTime) {
}
