package io.temporal.app.messages.api;

import java.util.Date;

public record AccommodationView(String id, String name, Date startDate, Date endDate) {
}
