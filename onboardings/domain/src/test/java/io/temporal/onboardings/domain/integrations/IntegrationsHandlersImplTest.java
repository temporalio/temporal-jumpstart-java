package io.temporal.onboardings.domain.integrations;

import io.temporal.client.WorkflowClient;
import io.temporal.onboardings.domain.clients.crm.CrmClient;
import io.temporal.onboardings.domain.messages.commands.RegisterCrmEntityRequest;
import io.temporal.testing.TestActivityEnvironment;
import io.temporal.testing.TestEnvironmentOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(
    classes = {
      IntegrationsHandlersImplTest.Configuration.class,
    })
// @ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@EnableAutoConfiguration()
@DirtiesContext
@ComponentScan(
    basePackageClasses = {
      IntegrationsHandlersImpl.class,
    })
public class IntegrationsHandlersImplTest {
  @Autowired ConfigurableApplicationContext applicationContext;

  @Autowired TestWorkflowEnvironment testWorkflowEnvironment;

  @Autowired WorkflowClient workflowClient;

  @Autowired IntegrationsHandlers sut;

  @MockBean CrmClient crmClient;

  @Value("${spring.temporal.workers[0].task-queue}")
  String taskQueue;

  TestActivityEnvironment testActivityEnvironment;

  @AfterEach
  public void after() {
    testWorkflowEnvironment.close();
  }

  @BeforeEach
  void beforeEach() {
    testActivityEnvironment =
        TestActivityEnvironment.newInstance(
            TestEnvironmentOptions.newBuilder().setUseTimeskipping(true).build());
    testActivityEnvironment.registerActivitiesImplementations(sut);

    applicationContext.start();
  }

  @Test
  public void registerCrmEntity_givenConnectivityProblem_shouldThrowServiceUnrecoverable() {
    var cmd =
        new RegisterCrmEntityRequest(UUID.randomUUID().toString(), UUID.randomUUID().toString());
    var stub = testActivityEnvironment.newActivityStub(IntegrationsHandlers.class);
    stub.registerCrmEntity(cmd);
  }

  @ComponentScan
  public static class Configuration {}
}
