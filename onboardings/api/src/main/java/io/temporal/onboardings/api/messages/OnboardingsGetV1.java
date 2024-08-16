package io.temporal.onboardings.api.messages;

public record OnboardingsGetV1(String id, String value, String approvalStatus, String comment) {}
