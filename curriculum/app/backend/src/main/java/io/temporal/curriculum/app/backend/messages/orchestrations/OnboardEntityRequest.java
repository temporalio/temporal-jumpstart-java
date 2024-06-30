package io.temporal.curriculum.app.backend.messages.orchestrations;

public record OnboardEntityRequest(
    String id,
    String value,
    int completionTimeoutSeconds,
    String deputyOwnerEmail,
    boolean skipApproval) {}
