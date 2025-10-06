package io.temporal.inframanager.domain.workflows;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowFailedException;
import io.temporal.client.WorkflowOptions;
import io.temporal.common.RetryOptions;
import io.temporal.failure.ApplicationFailure;
import io.temporal.inframanager.domain.DomainConfig;
import io.temporal.inframanager.domain.io.temporal.inframanager.domain.messages.Errors;
import io.temporal.inframanager.domain.io.temporal.inframanager.domain.messages.Workflows;
import io.temporal.testing.TestWorkflowEnvironment;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
    classes = {
      InfraSpaceTests.Configuration.class,
    })
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@EnableAutoConfiguration()
@DirtiesContext
@ActiveProfiles("test")
@Import(DomainConfig.class)
public class InfraSpaceTests {
  @Autowired ConfigurableApplicationContext applicationContext;

  @Autowired TestWorkflowEnvironment testWorkflowEnvironment;

  @Autowired WorkflowClient workflowClient;

  @Value("${spring.temporal.workers[0].task-queue}")
  String taskQueue;

  @BeforeEach
  void beforeEach() {
    applicationContext.start();
  }

  @Test
  public void givenInvalidArgs_itShouldFail() {
    var args = new Workflows.StartInfraSpaceRequest(UUID.randomUUID().toString());
    InfraSpace sut =
        workflowClient.newWorkflowStub(
            InfraSpace.class,
            WorkflowOptions.newBuilder()
                .setRetryOptions(RetryOptions.newBuilder().setMaximumAttempts(1).build())
                .setWorkflowId(args.getName())
                .setTaskQueue(taskQueue)
                .build());

    /*
       // async execution
       var exec =WorkflowClient.start(sut::execute, args);
       var stub = WorkflowStub.fromTyped(exec);
       var result = stub.getResult()
    */
    var e =
        Assertions.assertThrows(
            WorkflowFailedException.class,
            () -> {
              sut.execute(args);
            });
    Assertions.assertInstanceOf(ApplicationFailure.class, e.getCause());
    Assertions.assertEquals(
        Errors.INVALID_ARGS.name(), ((ApplicationFailure) e.getCause()).getType());
  }

  @ComponentScan
  public static class Configuration {
    //        @MockBean private CrmClient crmListener;
    //
    //        @MockBean private EmailClient emailClient;
    //
    //        @Primary
    //        @Bean
    //        public CrmClient getCrmListener() {
    //            return crmListener;
    //        }
    //
    //        @Primary
    //        @Bean
    //        EmailClient getEmailClient() {
    //            return emailClient;
    //        }
  }
}
