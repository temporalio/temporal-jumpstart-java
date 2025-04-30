package io.temporal.onboardings.domain.messages.commands;

import io.temporal.onboardings.domain.messages.queries.EntityOnboardingState;

public record SyncToStorageRequest(EntityOnboardingState onboardingState) {}
