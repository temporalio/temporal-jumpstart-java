package io.temporal.onboardings.workers.temporal;

import io.temporal.onboardings.domain.clients.ClientsConfiguration;
import io.temporal.onboardings.domain.integrations.IntegrationsConfiguration;
import io.temporal.onboardings.domain.notifications.NotificationsConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({
  NotificationsConfiguration.class,
  IntegrationsConfiguration.class,
  ClientsConfiguration.class
})
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
