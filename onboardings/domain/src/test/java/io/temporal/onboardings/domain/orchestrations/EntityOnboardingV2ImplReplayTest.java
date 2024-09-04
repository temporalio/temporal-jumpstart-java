package io.temporal.onboardings.domain.orchestrations;

import static org.mockito.Mockito.*;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.onboardings.domain.DomainConfig;
import io.temporal.onboardings.domain.integrations.IntegrationsHandlers;
import io.temporal.onboardings.domain.messages.commands.ApproveEntityRequest;
import io.temporal.onboardings.domain.messages.orchestrations.OnboardEntityRequest;
import io.temporal.onboardings.domain.notifications.NotificationsHandlers;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.testing.WorkflowReplayer;
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
      EntityOnboardingMockedActivityTest.Configuration.class,
    })
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@EnableAutoConfiguration()
@DirtiesContext
@ActiveProfiles("test")
@Import(DomainConfig.class)
public class EntityOnboardingV2ImplReplayTest {
  @Autowired ConfigurableApplicationContext applicationContext;

  @Autowired TestWorkflowEnvironment testWorkflowEnvironment;

  @Autowired WorkflowClient workflowClient;

  @MockBean IntegrationsHandlers integrationsHandlers;

  @Autowired NotificationsHandlers notificationsHandlers;

  @Value("${spring.temporal.workers[0].task-queue}")
  String taskQueue;

  @Value("${spring.temporal.workers[1].task-queue}")
  String taskQueueV2;

  @BeforeEach
  void beforeEach() {
    applicationContext.start();
  }

  // state verification
  @Test
  public void givenValidArgsWithOwnerApprovalNoDeputyOwner_whenApproved_itShouldBeApproved() {
    String wfId = UUID.randomUUID().toString();
    String wfId2 = UUID.randomUUID().toString();
    var args = new OnboardEntityRequest(wfId, UUID.randomUUID().toString(), 4, null, false);
    EntityOnboarding source =
        workflowClient.newWorkflowStub(
            EntityOnboarding.class,
            WorkflowOptions.newBuilder().setWorkflowId(wfId).setTaskQueue(taskQueue).build());
    WorkflowClient.start(source::execute, args);
    testWorkflowEnvironment.sleep(Duration.ofSeconds(1));
    source.approve(new ApproveEntityRequest("nocomment"));
    testWorkflowEnvironment.sleep(Duration.ofSeconds(1));
    Assertions.assertDoesNotThrow(() -> source.execute(args));
    var history = workflowClient.fetchHistory(args.id());
    Assertions.assertDoesNotThrow(
        () -> {
          WorkflowReplayer.replayWorkflowExecution(history, EntityOnboardingV2Impl.class);
        });
  }

  @Test
  public void
      POTATO_givenValidArgsWithOwnerApprovalNoDeputyOwner_whenApproved_itShouldBeApproved() {
    String wfId = UUID.randomUUID().toString();
    String wfId2 = UUID.randomUUID().toString();
    var args = new OnboardEntityRequest(wfId, UUID.randomUUID().toString(), 4, null, false);
    //    Potato source =
    //        workflowClient.newWorkflowStub(
    //            Potato.class,
    //            WorkflowOptions.newBuilder().setWorkflowId(wfId).setTaskQueue(taskQueue).build());
    var wf =
        workflowClient.newUntypedWorkflowStub(
            "Potato",
            WorkflowOptions.newBuilder().setWorkflowId(wfId).setTaskQueue(taskQueue).build());
    wf.start(args);
    //    WorkflowClient.start("Potato", args);
    //    WorkflowClient.start(source::execute, args);
    testWorkflowEnvironment.sleep(Duration.ofSeconds(1));
    wf.signal("approve", new ApproveEntityRequest("nocomment"));
    //    source.approve(new ApproveEntityRequest("nocomment"));
    testWorkflowEnvironment.sleep(Duration.ofSeconds(1));
    //    Assertions.assertDoesNotThrow(() -> source.execute(args));
    wf.getResult(Void.class);
    //    source.execute(args);

    var history = workflowClient.fetchHistory(args.id());
    Assertions.assertDoesNotThrow(
        () -> {
          WorkflowReplayer.replayWorkflowExecution(history, EntityOnboardingV2Impl.class);
        });
  }

  @Test
  public void givenValidArgsWithOwnerApprovalNoDeputyOwner_whenRejected_itShouldBeRejected() {}

  @Test
  public void
      givenValidArgsWithOwnerApprovalAndDeputyOwnerWhenApprovalWindowTimesOut_itShouldAllowApprovalByDeputy() {}
}
