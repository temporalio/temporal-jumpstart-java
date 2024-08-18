package io.temporal.onboardings.domain.orchestrations;

import static org.mockito.Mockito.*;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowFailedException;
import io.temporal.client.WorkflowOptions;
import io.temporal.failure.ApplicationFailure;
import io.temporal.onboardings.domain.DomainConfig;
import io.temporal.onboardings.domain.integrations.IntegrationsHandlers;
import io.temporal.onboardings.domain.messages.commands.ApproveEntityRequest;
import io.temporal.onboardings.domain.messages.orchestrations.Errors;
import io.temporal.onboardings.domain.messages.orchestrations.OnboardEntityRequest;
import io.temporal.onboardings.domain.messages.queries.EntityOnboardingState;
import io.temporal.onboardings.domain.messages.values.ApprovalStatus;
import io.temporal.onboardings.domain.notifications.NotificationsHandlers;
import io.temporal.testing.TestWorkflowEnvironment;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
    classes = {
      EntityOnboardingTest.Configuration.class,
    })
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@EnableAutoConfiguration()
@DirtiesContext
@ActiveProfiles("test")
@Import(DomainConfig.class)
public class EntityOnboardingTest {
  @Autowired ConfigurableApplicationContext applicationContext;

  @Autowired TestWorkflowEnvironment testWorkflowEnvironment;

  @Autowired WorkflowClient workflowClient;

  @MockBean CrmListener listener;

  @MockBean IntegrationsHandlers integrationsHandlers;

  @Autowired NotificationsHandlers notificationsHandlers;

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
  public void givenValidArgsWithOwnerApprovalNoDeputyOwner_itShouldOnboardTheEntity() {
    String wfId = UUID.randomUUID().toString();
    var args = new OnboardEntityRequest(wfId, UUID.randomUUID().toString(), 4, null, false);
    EntityOnboarding sut =
        workflowClient.newWorkflowStub(
            EntityOnboarding.class,
            WorkflowOptions.newBuilder().setWorkflowId(wfId).setTaskQueue(taskQueue).build());
    WorkflowClient.start(sut::execute, args);
    testWorkflowEnvironment.sleep(Duration.ofSeconds(1));
    sut.approve(new ApproveEntityRequest("nocomment"));
    System.out.printf("Type of mock is " + notificationsHandlers.getClass() + "\n");
    testWorkflowEnvironment.sleep(Duration.ofSeconds(1));

    verify(integrationsHandlers, times(1))
        .registerCrmEntity(
            argThat(arg -> arg.id().equals(wfId) && arg.value().equals(args.value())));
    EntityOnboardingState response = sut.getState();
    Assertions.assertEquals(response.approval().approvalStatus(), ApprovalStatus.APPROVED);
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
    Assertions.assertEquals(
        Errors.INVALID_ARGS.name(), ((ApplicationFailure) e.getCause()).getType());
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
    //    verify(cfg.integrationHandlers, times(1))
    //        .registerCrmEntity(
    //            argThat(
    //                arg -> {
    //                  return Objects.equals(args.id(), arg.id())
    //                      && Objects.equals(args.value(), arg.value());
    //                }));
  }

  @ComponentScan
  //  @TestConfiguration
  public static class Configuration {
    //        @MockBean private IntegrationsHandlers integrationsHandlersMock;
    //    @MockBean private NotificationsHandlers notificationsHandlersMock;
    //
    //    //    @Bean("integrations-handlers")
    //    //    public IntegrationsHandlers getIntegrationsHandlers() {
    //    //      return integrationsHandlersMock;
    //    //    }
    //
    //    @Primary
    //    @Bean("notifications-handlers")
    //    public NotificationsHandlers getNotificationsHandlers() {
    //      return notificationsHandlersMock;
    //    }
    //    @MockBean(name = "integrations-handlers")
    //    private IntegrationsHandlers integrationHandlers;
    //
    //    @MockBean(name = "notifications-handlers")
    //    private NotificationsHandlers notificationsHandlers;
    //
    //    @Bean
    //    public IntegrationsHandlers getIntegrationsTestHandlersBean() {
    //      Mockito.doNothing()
    //          .when(integrationHandlers)
    //          .registerCrmEntity(any(RegisterCrmEntityRequest.class));
    //      return integrationHandlers;
    //    }
    //
    //    @Bean
    //    public NotificationsHandlers getNotificationsTestHandlers() {
    //      Mockito.doNothing()
    //          .when(notificationsHandlers)
    //          .requestDeputyOwnerApproval(any(RequestDeputyOwnerApprovalRequest.class));
    //      return notificationsHandlers;
    //    }
  }
}
