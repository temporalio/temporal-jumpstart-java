package io.temporal.onboardings.api.messages;

public record OnboardingsGetV2(String id, String value, String approvalStatus, String comment) {}
