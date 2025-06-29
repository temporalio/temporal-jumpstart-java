package io.temporal.fsi.web.controllers;

import io.temporal.api.enums.v1.WorkflowIdConflictPolicy;
import io.temporal.api.enums.v1.WorkflowIdReusePolicy;
import io.temporal.client.*;
import io.temporal.fsi.api.applications.v1.RegisterUserRequest;
import io.temporal.fsi.api.applications.v1.StartUserRegistrationsRequest;
import io.temporal.fsi.api.web.v1.RegistrationsPut;
import io.temporal.fsi.domain.applications.workflows.userRegistrations.UserRegistrations;
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
            .setWorkflowId("users")
            .setRetryOptions(null)
            .setWorkflowIdReusePolicy(
                WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_REJECT_DUPLICATE)
            .setWorkflowIdConflictPolicy(
                WorkflowIdConflictPolicy.WORKFLOW_ID_CONFLICT_POLICY_USE_EXISTING)
            .build();
    var start = StartUserRegistrationsRequest.newBuilder().setName("users").build();
    var registration = RegisterUserRequest.newBuilder().setEmail(params.getEmail()).build();
    var workflowStub = temporalClient.newWorkflowStub(UserRegistrations.class, options);

    try {
      WorkflowClient.startUpdateWithStart(
          workflowStub::registerUser,
          registration,
          UpdateOptions.newBuilder().setWaitForStage(WorkflowUpdateStage.COMPLETED).build(),
          new WithStartWorkflowOperation<>(workflowStub::start, start));
    } catch (Exception e) {
      logger.error("Error starting workflow", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }
}
