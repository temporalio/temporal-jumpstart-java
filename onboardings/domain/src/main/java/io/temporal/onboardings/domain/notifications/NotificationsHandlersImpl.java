package io.temporal.onboardings.domain.notifications;

import io.temporal.onboardings.domain.clients.email.EmailClient;
import io.temporal.onboardings.domain.messages.commands.RequestDeputyOwnerApprovalRequest;
import org.springframework.stereotype.Component;

@Component("notifications-handlers")
public class NotificationsHandlersImpl implements NotificationsHandlers {
  private EmailClient emailClient;

  public NotificationsHandlersImpl(EmailClient emailClient) {
    this.emailClient = emailClient;
  }

  @Override
  public void requestDeputyOwnerApproval(RequestDeputyOwnerApprovalRequest cmd) {
    emailClient.sendEmail(
        cmd.deputyOwnerEmail(),
        "An approval has been requested at http://localhost:3030/api/onboardings/" + cmd.id());
  }
}
