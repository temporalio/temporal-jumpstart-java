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
import io.temporal.failure.ActivityFailure;
import io.temporal.failure.ApplicationFailure;
import io.temporal.onboardings.domain.DomainConfig;
import io.temporal.onboardings.domain.clients.crm.CrmClient;
import io.temporal.onboardings.domain.clients.email.EmailClient;
import io.temporal.onboardings.domain.messages.commands.ApproveEntityRequest;
import io.temporal.onboardings.domain.messages.commands.RejectEntityRequest;
import io.temporal.onboardings.domain.messages.orchestrations.Errors;
import io.temporal.onboardings.domain.messages.orchestrations.OnboardEntityRequest;
import io.temporal.onboardings.domain.messages.queries.EntityOnboardingState;
import io.temporal.onboardings.domain.messages.values.ApprovalStatus;
import io.temporal.testing.TestWorkflowEnvironment;
import java.net.ConnectException;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;

@SpringBootTest(
    classes = {
      EntityOnboardingMockedDependencyTest.Configuration.class,
    })
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@EnableAutoConfiguration()
@DirtiesContext
@ActiveProfiles("test")
@Import(DomainConfig.class)
public class EntityOnboardingMockedDependencyTest {
  @Autowired ConfigurableApplicationContext applicationContext;

  @Autowired TestWorkflowEnvironment testWorkflowEnvironment;

  @Autowired WorkflowClient workflowClient;

  @Autowired CrmClient crmClient;

  @Autowired EmailClient emailClient;

  @Value("${spring.temporal.workers[0].task-queue}")
  String taskQueue;

  @BeforeEach
  void beforeEach() {
    applicationContext.start();
  }

  @Test
  public void givenValidArgsWithOwnerApprovalNoDeputyOwner_whenApproved_itShouldBeApproved() {
    var args =
        new OnboardEntityRequest(
            UUID.randomUUID().toString(), UUID.randomUUID().toString(), 4, null, false);
    EntityOnboarding sut =
        workflowClient.newWorkflowStub(
            EntityOnboarding.class,
            WorkflowOptions.newBuilder().setWorkflowId(args.id()).setTaskQueue(taskQueue).build());
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

    var args =
        new OnboardEntityRequest(
            UUID.randomUUID().toString(), UUID.randomUUID().toString(), 4, null, false);

    when(crmClient.getCustomerById(args.id()))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

    EntityOnboarding sut =
        workflowClient.newWorkflowStub(
            EntityOnboarding.class,
            WorkflowOptions.newBuilder().setWorkflowId(args.id()).setTaskQueue(taskQueue).build());
    WorkflowClient.start(sut::execute, args);
    testWorkflowEnvironment.sleep(Duration.ofSeconds(1));
    sut.approve(new ApproveEntityRequest("nocomment"));
    testWorkflowEnvironment.sleep(Duration.ofSeconds(1));

    try {
      verify(crmClient, times(1)).registerCustomer(eq(args.id()), eq(args.value()));
    } catch (ConnectException ignored) {
    }
    verify(emailClient, never()).sendEmail(any(), any());
  }

  // state verification
  @Test
  public void givenValidArgsWithOwnerApprovalNoDeputyOwner_whenRejected_itShouldBeRejected() {
    var args =
        new OnboardEntityRequest(
            UUID.randomUUID().toString(), UUID.randomUUID().toString(), 4, null, false);
    EntityOnboarding sut =
        workflowClient.newWorkflowStub(
            EntityOnboarding.class,
            WorkflowOptions.newBuilder().setWorkflowId(args.id()).setTaskQueue(taskQueue).build());
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
    try {
      verify(crmClient, never()).registerCustomer(any(), any());
    } catch (ConnectException ignored) {
    }
    verify(emailClient, never()).sendEmail(any(), any());
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
    when(crmClient.getCustomerById(args.id()))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
    EntityOnboarding sut =
        workflowClient.newWorkflowStub(
            EntityOnboarding.class,
            WorkflowOptions.newBuilder().setWorkflowId(args.id()).setTaskQueue(taskQueue).build());

    WorkflowClient.start(sut::execute, args);
    testWorkflowEnvironment.sleep(Duration.ofSeconds(completionTimeoutSeconds));
    sut.approve(new ApproveEntityRequest("nocomment"));
    testWorkflowEnvironment.sleep(Duration.ofSeconds(1));
    try {
      verify(crmClient, times(1)).registerCustomer(eq(args.id()), eq(args.value()));
    } catch (ConnectException ignored) {
    }
    verify(emailClient, times(1)).sendEmail(eq(args.deputyOwnerEmail()), any());
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

  // behavior verification
  @Test
  public void execute_givenErmClientOutage_itShouldFailWorkflow2() {

    var args =
        new OnboardEntityRequest(
            UUID.randomUUID().toString(), UUID.randomUUID().toString(), 4, null, false);

    when(crmClient.getCustomerById(args.id()))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
    try {
      Mockito.doThrow(ConnectException.class).when(crmClient).registerCustomer(any(), any());
    } catch (ConnectException e) {
      throw new RuntimeException(e);
    }
    EntityOnboarding sut =
        workflowClient.newWorkflowStub(
            EntityOnboarding.class,
            WorkflowOptions.newBuilder().setWorkflowId(args.id()).setTaskQueue(taskQueue).build());
    var foo = WorkflowClient.start(sut::execute, args);
    testWorkflowEnvironment.sleep(Duration.ofSeconds(1));
    sut.approve(new ApproveEntityRequest("nocomment"));
    testWorkflowEnvironment.sleep(Duration.ofSeconds(1));
    var e =
        Assertions.assertThrows(
            WorkflowFailedException.class,
            () -> {
              sut.execute(args);
            });

    var ae = Assertions.assertInstanceOf(ActivityFailure.class, e.getCause());
    var afe = Assertions.assertInstanceOf(ApplicationFailure.class, ae.getCause());

    Assertions.assertEquals(Errors.SERVICE_UNRECOVERABLE.name(), afe.getType());
  }

  @ComponentScan
  public static class Configuration {
    @MockBean private CrmClient crmListener;

    @MockBean private EmailClient emailClient;

    @Primary
    @Bean
    public CrmClient getCrmListener() {
      return crmListener;
    }

    @Primary
    @Bean
    EmailClient getEmailClient() {
      return emailClient;
    }
  }
}
