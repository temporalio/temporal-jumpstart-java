package io.temporal.onboardings.domain.notifications;

import io.temporal.activity.ActivityInterface;
import io.temporal.onboardings.domain.messages.commands.RequestDeputyOwnerApprovalRequest;

@ActivityInterface
public interface NotificationsHandlers {
  void requestDeputyOwnerApproval(RequestDeputyOwnerApprovalRequest cmd);
}
