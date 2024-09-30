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

package io.temporal.onboardings.api.controllers;

import io.temporal.api.common.v1.Payload;
import io.temporal.api.common.v1.Payloads;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.enums.v1.WorkflowIdReusePolicy;
import io.temporal.api.workflowservice.v1.DescribeWorkflowExecutionRequest;
import io.temporal.api.workflowservice.v1.DescribeWorkflowExecutionResponse;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowExecutionAlreadyStarted;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.common.converter.DefaultDataConverter;
import io.temporal.onboardings.api.messages.OnboardingsGetV0;
import io.temporal.onboardings.api.messages.OnboardingsPutV0;
import io.temporal.onboardings.domain.messages.orchestrations.OnboardEntityRequest;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v0/onboardings")
public class OnboardingsControllerV0 {

  Logger logger = LoggerFactory.getLogger(OnboardingsControllerV0.class);
  @Autowired WorkflowClient temporalClient;

  @Value("${spring.curriculum.task-queue}")
  String taskQueue;

  @GetMapping("/{id}")
  public ResponseEntity<OnboardingsGetV0> onboardingGet(@PathVariable("id") String id) {
    // this is relatively advanced use of the TemporalClient but is shown here to
    // illustrate how to interact with the lower-level gRPC API for extracting details
    // about the WorkflowExecution.
    // We will be replacing this usage with a `Query` invocation to be simpler and more explicit.
    // This module will not overly explain this interaction but will be valuable later when we
    // want to reason about our Executions with more detail.
    var svc = this.temporalClient.getWorkflowServiceStubs();

    WorkflowExecution execution = WorkflowExecution.newBuilder().setWorkflowId(id).build();
    DescribeWorkflowExecutionResponse desc =
        svc.blockingStub()
            .describeWorkflowExecution(
                DescribeWorkflowExecutionRequest.newBuilder()
                    .setExecution(execution)
                    .setNamespace("default")
                    .build());
    var status = desc.getWorkflowExecutionInfo().getStatus();
    var history = this.temporalClient.fetchHistory(id);
    Payloads payloads =
        history.getHistory().getEvents(0).getWorkflowExecutionStartedEventAttributes().getInput();
    OnboardEntityRequest sentRequest = null;
    for (Payload payload : payloads.getPayloadsList()) {
      // using default data converter..assumes MigrateableWorkflowParams type
      // note if you use custom data converter you would need use it instead of default
      sentRequest =
          DefaultDataConverter.newDefaultInstance()
              .fromPayload(payload, OnboardEntityRequest.class, OnboardEntityRequest.class);
    }
    var get = new OnboardingsGetV0(sentRequest.id(), status.toString(), sentRequest);
    return ResponseEntity.ok(get);
  }

  @PutMapping(
      value = "/{id}",
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  ResponseEntity<String> onboardingPut(
      @PathVariable String id, @RequestBody OnboardingsPutV0 params) {

    return startOnboardEntity(id, params);
  }

  // https://jumpstart.temporal.io/starters
  private ResponseEntity<String> startOnboardEntity(String id, OnboardingsPutV0 params) {

    //    // Let's start a workflow!
    //    final WorkflowOptions options =
    //            WorkflowOptions.newBuilder()
    //                    // what's a task queue for?
    //                    .setTaskQueue(taskQueue)
    //                    // what should I use as naming convention?
    //                    .setWorkflowId(id)
    //                    // what do all these enum options mean in practice?
    //
    // .setWorkflowIdReusePolicy(WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_ALLOW_DUPLICATE_FAILED_ONLY)
    //                    // hmmm, not sure why if I need this too
    //
    // .setWorkflowIdConflictPolicy(WorkflowIdConflictPolicy.WORKFLOW_ID_CONFLICT_POLICY_FAIL)
    //                    // sounds good...i mean, shouldn't I retry a workflow?
    //                    .setRetryOptions(null)
    //                    // when should I propagate a thing? when shouldn't I?
    //                    .setContextPropagators(null)
    //                    // i'm usually an eager person, why would I possibly want to disable this?
    //                    .setDisableEagerExecution(true)
    //                    // who gets the memo and how can they see it? what's it for?
    //                    .setMemo(null)
    //                    // why would I use this instead just use a timer on the inside of my
    // workflow?
    //                    .setStartDelay(Duration.ofSeconds(30))
    //                    // i'm going to use search attributes for my UI; does that make sense?
    //                    .setTypedSearchAttributes(null)
    //                    // i'd like to control how long a thing runs, this is probably it
    //                    .setWorkflowRunTimeout(Duration.ofSeconds(60))
    //                    // wait - how is this one different from the other timeout?
    //                    .setWorkflowExecutionTimeout(Duration.ofMinutes(1000))
    //                    // another timeout!; now I'm not sure how to limit how long a thing
    // runs...
    //                    .setWorkflowTaskTimeout(Duration.ofMinutes(60))
    //                    // i heard Temporal Schedules crush cron...i'll ignore this I guess
    //                    .setCronSchedule("* :) ^--^")
    //                    // whew...I hope I did this right
    //                    .build();

    final WorkflowOptions options =
        WorkflowOptions.newBuilder()
            .setTaskQueue(taskQueue)

            // BestPractice: WorkflowIds should have business meaning.
            // Details: This identifier can be an AccountID, SessionID, etc.
            // 1. Prefer _pushing_ an WorkflowID down instead of retrieving after-the-fact.
            // 2. Acquaint your self with the "Workflow ID Reuse Policy" to fit your use case
            // Reference: https://docs.temporal.io/workflows#workflow-id-reuse-policy
            .setWorkflowId(id)
            // BestPractice: Do not fail a workflow on intermittent (eg bug) errors; prefer handling
            // failures at the Activity level within the Workflow.
            // Details: A Workflow will very rarely need one to specify a RetryPolicy when starting
            // a Workflow and we strongly discourage it.
            // Only Exceptions that inherit from `FailureException` will cause a RetryPolicy to be
            // enforced. Other Exceptions will cause the WorkflowTask
            // to be rescheduled so that Workflows can continue to make progress once
            // repaired/redeployed with corrections.
            .setRetryOptions(null)
            // Our requirements state that we want to allow the same WorkflowID if prior attempts
            // were Canceled.
            // Therefore, we are using this Policy that will reject duplicates unless previous
            // attempts did not reach terminal state as `Completed'.
            .setWorkflowIdReusePolicy(
                WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_ALLOW_DUPLICATE_FAILED_ONLY)
            .build();
    WorkflowStub workflowStub =
        temporalClient.newUntypedWorkflowStub("WorkflowDefinitionDoesntExistYet", options);

    var wfArgs = new OnboardEntityRequest(params.id(), params.value(), 7 * 86400, null, false);
    // Start the workflow execution.
    try {
      var run = workflowStub.start(wfArgs);
      var headers = new HttpHeaders();
      headers.setLocation(URI.create(String.format("/api/v0/onboardings/%s", id)));
      return new ResponseEntity<>(HttpStatus.ACCEPTED);
    } catch (WorkflowExecutionAlreadyStarted was) {
      logger.info("Workflow execution already started: {}", id);
      return new ResponseEntity<>(HttpStatus.ACCEPTED);
    } catch (Exception e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
