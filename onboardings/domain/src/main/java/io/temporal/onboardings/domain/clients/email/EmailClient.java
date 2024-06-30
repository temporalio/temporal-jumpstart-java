package io.temporal.onboardings.domain.clients.email;

public interface EmailClient {
  void sendEmail(String email, String body);
}
