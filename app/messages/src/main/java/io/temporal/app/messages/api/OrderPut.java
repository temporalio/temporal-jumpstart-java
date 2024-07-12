package io.temporal.app.messages.api;

import java.util.List;

public record OrderPut(String id, String userId, List<FlightView> flights, List<AccommodationView> accommodations, List<TaxiView> taxis) {}
