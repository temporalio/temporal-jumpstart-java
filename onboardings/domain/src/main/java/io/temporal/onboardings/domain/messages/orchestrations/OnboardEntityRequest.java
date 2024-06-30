package io.temporal.onboardings.domain.messages.orchestrations;

public record OnboardEntityRequest(
    String id,
    String value,
    int completionTimeoutSeconds,
    String deputyOwnerEmail,
    boolean skipApproval) {}
