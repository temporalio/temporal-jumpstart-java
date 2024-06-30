package io.temporal.onboardings.domain.messages.queries;

import io.temporal.onboardings.domain.messages.values.Approval;

public record EntityOnboardingState(String id, String currentValue, Approval approval) {}
