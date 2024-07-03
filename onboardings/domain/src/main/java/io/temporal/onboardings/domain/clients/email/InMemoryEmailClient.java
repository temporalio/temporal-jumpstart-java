package io.temporal.onboardings.domain.clients.email;

import io.temporal.onboardings.domain.clients.repositories.RepositoriesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InMemoryEmailClient implements EmailClient {

  Logger logger = LoggerFactory.getLogger(InMemoryEmailClient.class);
  RepositoriesClient repositoriesClient;

  public InMemoryEmailClient(RepositoriesClient repositoriesClient) {
    this.repositoriesClient = repositoriesClient;
  }

  @Override
  public void sendEmail(String email, String body) {
    logger.info("sending email for {} with {}", email, body);
    repositoriesClient.saveEmail(email, body);
  }
}
