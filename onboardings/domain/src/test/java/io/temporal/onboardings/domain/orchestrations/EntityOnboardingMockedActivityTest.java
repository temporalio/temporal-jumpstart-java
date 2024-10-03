/*
 * MIT License
 *
 * Copyright (c) 2024 temporal.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.temporal.onboardings.domain.orchestrations;

import static org.mockito.Mockito.*;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowFailedException;
import io.temporal.client.WorkflowOptions;
import io.temporal.failure.ApplicationFailure;
import io.temporal.onboardings.domain.DomainConfig;
import io.temporal.onboardings.domain.integrations.IntegrationsHandlers;
import io.temporal.onboardings.domain.messages.commands.ApproveEntityRequest;
import io.temporal.onboardings.domain.messages.commands.RejectEntityRequest;
import io.temporal.onboardings.domain.messages.orchestrations.Errors;
import io.temporal.onboardings.domain.messages.orchestrations.OnboardEntityRequest;
import io.temporal.onboardings.domain.messages.queries.EntityOnboardingState;
import io.temporal.onboardings.domain.messages.values.ApprovalStatus;
import io.temporal.onboardings.domain.notifications.NotificationsHandlers;
import io.temporal.testing.TestWorkflowEnvironment;
import java.time.Duration;
import java.util.Objects;
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
public class EntityOnboardingMockedActivityTest {
  @Autowired ConfigurableApplicationContext applicationContext;

  @Autowired TestWorkflowEnvironment testWorkflowEnvironment;

  @Autowired WorkflowClient workflowClient;

  @MockBean IntegrationsHandlers integrationsHandlers;

  @Autowired NotificationsHandlers notificationsHandlers;

  @Value("${spring.temporal.workers[0].task-queue}")
  String taskQueue;

  @BeforeEach
  void beforeEach() {
    applicationContext.start();
  }

  // state verification
  @Test
  public void givenValidArgsWithOwnerApprovalNoDeputyOwner_whenApproved_itShouldBeApproved() {
    String wfId = UUID.randomUUID().toString();
    var args = new OnboardEntityRequest(wfId, UUID.randomUUID().toString(), 4, null, false);
    EntityOnboarding sut =
        workflowClient.newWorkflowStub(
            EntityOnboarding.class,
            WorkflowOptions.newBuilder().setWorkflowId(wfId).setTaskQueue(taskQueue).build());
    WorkflowClient.start(sut::execute, args);
    testWorkflowEnvironment.sleep(Duration.ofSeconds(1));
    sut.approve(new ApproveEntityRequest("nocomment"));
    testWorkflowEnvironment.sleep(Duration.ofSeconds(1));
    EntityOnboardingState response = sut.getState();
    Assertions.assertEquals(response.approval().approvalStatus(), ApprovalStatus.APPROVED);
  }

  // behavior verification
  @Test
  public void
      givenValidArgsWithOwnerApprovalNoDeputyOwner_whenApproved_itShouldRegisterTheEntity() {
    String wfId = UUID.randomUUID().toString();
    var args = new OnboardEntityRequest(wfId, UUID.randomUUID().toString(), 4, null, false);
    EntityOnboarding sut =
        workflowClient.newWorkflowStub(
            EntityOnboarding.class,
            WorkflowOptions.newBuilder().setWorkflowId(args.id()).setTaskQueue(taskQueue).build());
    WorkflowClient.start(sut::execute, args);
    testWorkflowEnvironment.sleep(Duration.ofSeconds(1));
    sut.approve(new ApproveEntityRequest("nocomment"));
    testWorkflowEnvironment.sleep(Duration.ofSeconds(1));

    verify(integrationsHandlers, times(1))
        .registerCrmEntity(
            argThat(
                inputCall ->
                    Objects.equals(inputCall.id(), args.id())
                        && Objects.equals(inputCall.value(), args.value())));
    verify(notificationsHandlers, never()).requestDeputyOwnerApproval(any());
  }
  // state verification
  @Test
  public void givenValidArgsWithOwnerApprovalNoDeputyOwner_whenRejected_itShouldBeRejected() {
    String wfId = UUID.randomUUID().toString();
    var args = new OnboardEntityRequest(wfId, UUID.randomUUID().toString(), 4, null, false);
    EntityOnboarding sut =
        workflowClient.newWorkflowStub(
            EntityOnboarding.class,
            WorkflowOptions.newBuilder().setWorkflowId(wfId).setTaskQueue(taskQueue).build());
    WorkflowClient.start(sut::execute, args);
    testWorkflowEnvironment.sleep(Duration.ofSeconds(1));
    sut.reject(new RejectEntityRequest("nocomment"));
    testWorkflowEnvironment.sleep(Duration.ofSeconds(1));
    EntityOnboardingState response = sut.getState();
    Assertions.assertEquals(response.approval().approvalStatus(), ApprovalStatus.REJECTED);
  }
  // behavior verification
  @Test
  public void
      givenValidArgsWithOwnerApprovalNoDeputyOwner_whenRejected_itShouldNotRegisterTheEntity() {
    String wfId = UUID.randomUUID().toString();
    var args = new OnboardEntityRequest(wfId, UUID.randomUUID().toString(), 4, null, false);
    EntityOnboarding sut =
        workflowClient.newWorkflowStub(
            EntityOnboarding.class,
            WorkflowOptions.newBuilder().setWorkflowId(args.id()).setTaskQueue(taskQueue).build());
    WorkflowClient.start(sut::execute, args);
    testWorkflowEnvironment.sleep(Duration.ofSeconds(1));
    sut.reject(new RejectEntityRequest("nocomment"));
    testWorkflowEnvironment.sleep(Duration.ofSeconds(1));

    verifyNoInteractions(integrationsHandlers);
    verify(notificationsHandlers, never()).requestDeputyOwnerApproval(any());
  }

  @Test
  public void
      givenValidArgsWithOwnerApprovalAndDeputyOwnerWhenApprovalWindowTimesOut_itShouldAllowApprovalByDeputy() {
    int completionTimeoutSeconds = 30;
    var args =
        new OnboardEntityRequest(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            completionTimeoutSeconds,
            "deputydawg@example.com",
            false);
    EntityOnboarding sut =
        workflowClient.newWorkflowStub(
            EntityOnboarding.class,
            WorkflowOptions.newBuilder().setWorkflowId(args.id()).setTaskQueue(taskQueue).build());

    WorkflowClient.start(sut::execute, args);
    testWorkflowEnvironment.sleep(Duration.ofSeconds(completionTimeoutSeconds));
    sut.approve(new ApproveEntityRequest("from deputydawg@example.com"));
    testWorkflowEnvironment.sleep(Duration.ofSeconds(1));
    EntityOnboardingState response = sut.getState();
    Assertions.assertEquals(response.approval().approvalStatus(), ApprovalStatus.APPROVED);
  }

  @Test
  public void
      givenValidArgsWithOwnerApprovalAndDeputyOwnerWhenApprovalWindowTimesOut_shouldRequestDeputyOwnerApproval() {
    int completionTimeoutSeconds = 30;
    var args =
        new OnboardEntityRequest(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            completionTimeoutSeconds,
            "deputydawg@example.com",
            false);
    EntityOnboarding sut =
        workflowClient.newWorkflowStub(
            EntityOnboarding.class,
            WorkflowOptions.newBuilder().setWorkflowId(args.id()).setTaskQueue(taskQueue).build());

    WorkflowClient.start(sut::execute, args);
    testWorkflowEnvironment.sleep(Duration.ofSeconds(completionTimeoutSeconds));
    sut.approve(new ApproveEntityRequest("nocomment"));
    testWorkflowEnvironment.sleep(Duration.ofSeconds(1));
    verify(notificationsHandlers, times(1))
        .requestDeputyOwnerApproval(
            argThat(
                a ->
                    Objects.equals(a.id(), args.id())
                        && Objects.equals(a.deputyOwnerEmail(), args.deputyOwnerEmail())));
    verify(integrationsHandlers, times(1))
        .registerCrmEntity(
            argThat(
                a -> Objects.equals(a.id(), args.id()) && Objects.equals(a.value(), args.value())));
  }

  // state verification
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

  @ComponentScan
  public static class Configuration {
    @MockBean private NotificationsHandlers notificationsHandlersMock;

    @Primary
    @Bean("notifications-handlers")
    public NotificationsHandlers notificationsHandlers() {
      return notificationsHandlersMock;
    }
  }
}
