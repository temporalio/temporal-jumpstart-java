package io.temporal.curriculum.app.backend.orchestrations;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(
    classes = {
      EntityOnboardingTest.Configuration.class,
    })
// @ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnableAutoConfiguration()
@DirtiesContext
@ComponentScan(
    basePackageClasses = {
      OrchestrationsConfig.class,
    })
public class EntityOnboardingTest {
  @Autowired ConfigurableApplicationContext applicationContext;

  @Autowired TestWorkflowEnvironment testWorkflowEnvironment;

  @Autowired WorkflowClient workflowClient;

  @AfterEach
  public void after() {
    testWorkflowEnvironment.close();
  }

  @BeforeEach
  void beforeEach() {
    applicationContext.start();
  }

  @Test
  public void givenSimpleArgs_itShouldExecuteWorkflow() {
    String wfId = UUID.randomUUID().toString();
    EntityOnboarding sut =
        workflowClient.newWorkflowStub(
            EntityOnboarding.class,
            WorkflowOptions.newBuilder().setWorkflowId(wfId).setTaskQueue("test").build());
    WorkflowClient.execute(sut::execute);
  }

  @ComponentScan
  public static class Configuration {}
}
