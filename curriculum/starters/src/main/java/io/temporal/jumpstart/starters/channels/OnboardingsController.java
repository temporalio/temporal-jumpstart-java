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

package io.temporal.jumpstart.starters.channels;

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
import io.temporal.jumpstart.starters.messages.api.OnboardingsGet;
import io.temporal.jumpstart.starters.messages.api.OnboardingsPut;
import io.temporal.jumpstart.starters.messages.orchestrations.OnboardEntityRequest;
import java.net.URI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/onboardings")
public class OnboardingsController {

  @Autowired WorkflowClient temporalClient;

  @GetMapping("/{id}")
  public ResponseEntity<OnboardingsGet> GetOnboardingStatus(@PathVariable("id") String id) {
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
    var get = new OnboardingsGet(sentRequest.id(), status.toString(), sentRequest);
    return ResponseEntity.ok(get);
  }

  @PutMapping(
      value = "/{id}",
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  ResponseEntity<String> StartOnboardingAsync(
      @PathVariable String id, @RequestBody OnboardingsPut params) {

    final WorkflowOptions options =
        WorkflowOptions.newBuilder()
            .setTaskQueue("Onboardings")
            .setWorkflowId(id)
            .setRetryOptions(null)
            .setWorkflowIdReusePolicy(
                WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_REJECT_DUPLICATE)
            .build();
    WorkflowStub workflowStub =
        temporalClient.newUntypedWorkflowStub("WorkflowDefinitionDoesntExistYet", options);

    var wfArgs = new OnboardEntityRequest(params.id(), params.value());
    // Start the workflow execution.
    boolean alreadyStarted = false;
    try {
      var run = workflowStub.start(wfArgs);
      var headers = new HttpHeaders();
      headers.setLocation(URI.create(String.format("/api/onboardings/%s", id)));
      return new ResponseEntity<>(run.getRunId(), HttpStatus.ACCEPTED);
    } catch (WorkflowExecutionAlreadyStarted was) {
      alreadyStarted = true;
      return new ResponseEntity<>(HttpStatus.ACCEPTED);
    } catch (Exception e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
