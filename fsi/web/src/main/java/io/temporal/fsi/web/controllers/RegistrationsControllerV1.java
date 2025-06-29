package io.temporal.fsi.web.controllers;

import io.temporal.api.enums.v1.WorkflowIdReusePolicy;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.fsi.api.web.v1.RegistrationsPut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/registrations")
public class RegistrationsControllerV1 {
  Logger logger = LoggerFactory.getLogger(RegistrationsControllerV1.class);
  @Autowired WorkflowClient temporalClient;

  @Value("${spring.curriculum.task-queue}")
  String taskQueue;

  @PutMapping(
      value = "/{id}",
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  ResponseEntity<String> registrationsPut(
      @PathVariable String id, @RequestBody RegistrationsPut params) {
    // poor man's inspection to decide whether to update the entity or start a workflow
    // we could as easily check for WF existence first to decide which is best action to take
    final WorkflowOptions options =
        WorkflowOptions.newBuilder()
            .setTaskQueue(taskQueue)
            .setWorkflowId(id)
            .setRetryOptions(null)
            .setWorkflowIdReusePolicy(
                WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_REJECT_DUPLICATE)
            .build();

    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }
}
