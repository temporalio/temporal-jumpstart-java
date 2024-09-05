package io.temporal.onboardings.domain.orchestrations;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.onboardings.domain.DomainConfig;
import io.temporal.onboardings.domain.integrations.IntegrationsHandlers;
import io.temporal.onboardings.domain.messages.commands.ApproveEntityRequest;
import io.temporal.onboardings.domain.messages.orchestrations.OnboardEntityRequest;
import io.temporal.onboardings.domain.notifications.NotificationsHandlers;
import io.temporal.spring.boot.autoconfigure.template.WorkersTemplate;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.testing.WorkflowReplayer;
import io.temporal.worker.Worker;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class EntityOnboardingImplReplayTest {
  @Autowired ConfigurableApplicationContext applicationContext;

  @Autowired TestWorkflowEnvironment testWorkflowEnvironment;

  @Autowired WorkflowClient workflowClient;

  @MockBean IntegrationsHandlers integrationsHandlers;

  @Autowired NotificationsHandlers notificationsHandlers;

  @Value("${spring.temporal.workers[0].task-queue}")
  String taskQueue;

  @Autowired
  @Qualifier("temporalWorkersTemplate")
  private WorkersTemplate workersTemplate;

  @BeforeEach
  void beforeEach() {
    applicationContext.start();
  }

  // state verification
  @Test
  public void givenValidArgsWithOwnerApprovalNoDeputyOwner_whenApproved_itShouldBeApproved() {
    List<String> taskQueues =
        workersTemplate.getWorkers().stream()
            .map(Worker::getTaskQueue)
            .filter(s -> s.startsWith("replay_"))
            .toList();
    for (String tq : taskQueues) {
      String wfId = UUID.randomUUID().toString();
      String wfId2 = UUID.randomUUID().toString();
      var args = new OnboardEntityRequest(wfId, UUID.randomUUID().toString(), 4, null, false);
      EntityOnboarding source =
          workflowClient.newWorkflowStub(
              EntityOnboarding.class,
              WorkflowOptions.newBuilder().setWorkflowId(wfId).setTaskQueue(tq).build());
      WorkflowClient.start(source::execute, args);
      testWorkflowEnvironment.sleep(Duration.ofSeconds(1));
      source.approve(new ApproveEntityRequest("nocomment"));
      testWorkflowEnvironment.sleep(Duration.ofSeconds(1));
      Assertions.assertDoesNotThrow(() -> source.execute(args));
      var history = workflowClient.fetchHistory(args.id());
      Assertions.assertDoesNotThrow(
          () -> {
            WorkflowReplayer.replayWorkflowExecution(history, EntityOnboardingImpl.class);
          });
    }
  }

  @Test
  public void givenValidArgsWithOwnerApprovalNoDeputyOwner_whenRejected_itShouldBeRejected() {}

  @Test
  public void
      givenValidArgsWithOwnerApprovalAndDeputyOwnerWhenApprovalWindowTimesOut_itShouldAllowApprovalByDeputy() {}
}
