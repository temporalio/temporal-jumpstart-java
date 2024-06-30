package io.temporal.onboardings.domain.orchestrations;

import static org.mockito.Mockito.*;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowFailedException;
import io.temporal.client.WorkflowOptions;
import io.temporal.failure.ApplicationFailure;
import io.temporal.onboardings.domain.integrations.IntegrationsHandlers;
import io.temporal.onboardings.domain.messages.orchestrations.OnboardEntityRequest;
import io.temporal.testing.TestWorkflowEnvironment;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(
    classes = {
      EntityOnboardingTest.Configuration.class,
    })
// @ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
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

  @MockBean CrmListener listener;
  //  @MockBean IntegrationsHandlers integrationsHandlers;
  //
  //  @Bean
  //  public IntegrationsHandlers getIntegrationsHandlersBean() {
  //    return integrationsHandlers;
  //  }

  @Value("${spring.temporal.workers[0].task-queue}")
  String taskQueue;

  @AfterEach
  public void after() {
    testWorkflowEnvironment.close();
  }

  @BeforeEach
  void beforeEach() {
    applicationContext.start();
  }

  @Test
  public void givenValidArgs_itShouldExecuteWorkflow() {
    String wfId = UUID.randomUUID().toString();
    var args = new OnboardEntityRequest(wfId, UUID.randomUUID().toString(), 3, null, true);
    EntityOnboarding sut =
        workflowClient.newWorkflowStub(
            EntityOnboarding.class,
            WorkflowOptions.newBuilder().setWorkflowId(wfId).setTaskQueue(taskQueue).build());
    WorkflowClient.execute(() -> sut.execute(args));
  }

  @Test
  public void execute_givenInvalidArgs_itShouldFailWorkflow() {
    String wfId = UUID.randomUUID().toString();
    var args = new OnboardEntityRequest(wfId, "", 3, null, true);
    EntityOnboarding sut =
        workflowClient.newWorkflowStub(
            EntityOnboarding.class,
            WorkflowOptions.newBuilder().setWorkflowId(wfId).setTaskQueue(taskQueue).build());

    var e =
        Assertions.assertThrows(
            WorkflowFailedException.class,
            () -> {
              sut.execute(args);
            });
    Assertions.assertInstanceOf(ApplicationFailure.class, e.getCause());
    Assertions.assertEquals("invalid_args", ((ApplicationFailure) e.getCause()).getType());
  }

  @Test
  public void execute_givenHealthyService_registersCrmEntity() {
    var args =
        new OnboardEntityRequest(
            UUID.randomUUID().toString(), UUID.randomUUID().toString(), 3, null, true);
    EntityOnboarding sut =
        workflowClient.newWorkflowStub(
            EntityOnboarding.class,
            WorkflowOptions.newBuilder().setWorkflowId(args.id()).setTaskQueue(taskQueue).build());
    sut.execute(args);
    var cfg = new Configuration();
    verify(cfg.integrationHandlers, times(1))
        .registerCrmEntity(
            argThat(
                arg -> {
                  return Objects.equals(args.id(), arg.id())
                      && Objects.equals(args.value(), arg.value());
                }));
  }

  @ComponentScan
  public static class Configuration {
    @MockBean private IntegrationsHandlers integrationHandlers;

    @Bean
    public IntegrationsHandlers getIntegrationsHandlersBean() {
      return integrationHandlers;
    }
  }
}
