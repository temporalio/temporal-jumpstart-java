package io.temporal.onboardings.api.messages;

import io.temporal.onboardings.domain.messages.values.Approval;

public record OnboardingsPutV2(String id, String value, Approval approval) {}
