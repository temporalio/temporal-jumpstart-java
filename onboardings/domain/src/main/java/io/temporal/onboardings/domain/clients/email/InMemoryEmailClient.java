package io.temporal.onboardings.domain.clients.email;

import java.util.Dictionary;
import java.util.Hashtable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InMemoryEmailClient implements EmailClient {

  Logger logger = LoggerFactory.getLogger(InMemoryEmailClient.class);
  Dictionary<String, String> emails = new Hashtable<>();

  @Override
  public void sendEmail(String email, String body) {
    logger.info("sending email for {} with {}", email, body);
    emails.put(email, body);
  }
}
