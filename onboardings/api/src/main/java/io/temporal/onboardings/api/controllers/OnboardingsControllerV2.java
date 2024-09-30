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

import io.temporal.api.enums.v1.WorkflowIdReusePolicy;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowExecutionAlreadyStarted;
import io.temporal.client.WorkflowNotFoundException;
import io.temporal.client.WorkflowOptions;
import io.temporal.onboardings.api.messages.OnboardingsGetV2;
import io.temporal.onboardings.api.messages.OnboardingsPutV2;
import io.temporal.onboardings.domain.messages.commands.ApproveEntityRequest;
import io.temporal.onboardings.domain.messages.commands.RejectEntityRequest;
import io.temporal.onboardings.domain.messages.orchestrations.OnboardEntityRequest;
import io.temporal.onboardings.domain.messages.values.ApprovalStatus;
import io.temporal.onboardings.domain.orchestrations.EntityOnboarding;
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
@RequestMapping("/api/v2/onboardings")
public class OnboardingsControllerV2 {

  Logger logger = LoggerFactory.getLogger(OnboardingsControllerV2.class);
  @Autowired WorkflowClient temporalClient;

  @Value("${spring.curriculum.task-queue}")
  String taskQueue;

  @GetMapping("/{id}")
  public ResponseEntity<OnboardingsGetV2> onboardingGet(@PathVariable("id") String id) {
    try {
      var workflowStub = temporalClient.newWorkflowStub(EntityOnboarding.class, id);
      var state = workflowStub.getState();
      return new ResponseEntity<>(
          new OnboardingsGetV2(
              state.id(),
              state.currentValue(),
              state.approval().approvalStatus().name(),
              state.approval().comment(),
              "MISSING_EMAIL"),
          HttpStatus.OK);
    } catch (WorkflowNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @PutMapping(
      value = "/{id}",
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  ResponseEntity<String> onboardingPut(
      @PathVariable String id, @RequestBody OnboardingsPutV2 params) {
    // poor man's inspection to decide whether to update the entity or start a workflow
    // we could as easily check for WF existence first to decide which is best action to take
    if (params.approval().approvalStatus().equals(ApprovalStatus.PENDING)) {
      return startOnboardEntity(id, params);
    }

    // Signal our onboarding with the appropriate ApprovalStatus
    try {
      var wfStub = temporalClient.newWorkflowStub(EntityOnboarding.class, id);
      if (params.approval().approvalStatus().equals(ApprovalStatus.APPROVED)) {
        wfStub.approve(new ApproveEntityRequest(params.approval().comment()));
      } else if (params.approval().approvalStatus().equals(ApprovalStatus.REJECTED)) {
        wfStub.reject(new RejectEntityRequest(params.approval().comment()));
      } else {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
      }
    } catch (WorkflowNotFoundException e) {
      // you can receive this if the Workflow has Closed or simply is not there
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private ResponseEntity<String> startOnboardEntity(String id, OnboardingsPutV2 params) {
    final WorkflowOptions options =
        WorkflowOptions.newBuilder()
            .setTaskQueue(taskQueue)
            .setWorkflowId(id)
            .setRetryOptions(null)
            .setWorkflowIdReusePolicy(
                WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_REJECT_DUPLICATE)
            .build();
    var workflowStub = temporalClient.newWorkflowStub(EntityOnboarding.class, options);

    var wfArgs = new OnboardEntityRequest(params.id(), params.value(), 7 * 86400, null, false);
    // Start the workflow execution.
    try {
      var run = WorkflowClient.start(workflowStub::execute, wfArgs);
      var headers = new HttpHeaders();
      headers.setLocation(URI.create(String.format("/api/v2/onboardings/%s", id)));
      return new ResponseEntity<>(HttpStatus.ACCEPTED);
    } catch (WorkflowExecutionAlreadyStarted was) {
      logger.info("Workflow execution already started: {}", id);
      return new ResponseEntity<>(HttpStatus.ACCEPTED);
    } catch (Exception e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
