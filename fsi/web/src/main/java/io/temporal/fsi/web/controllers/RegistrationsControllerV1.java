package io.temporal.fsi.web.controllers;

import com.google.protobuf.util.JsonFormat;
import io.temporal.api.enums.v1.WorkflowIdConflictPolicy;
import io.temporal.client.*;
import io.temporal.fsi.api.applications.v1.*;
import io.temporal.fsi.api.web.v1.RegistrationGet;
import io.temporal.fsi.api.web.v1.RegistrationsPut;
import io.temporal.fsi.domain.applications.workflows.userRegistrations.UserRegistrations;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api/v1/registrations")
public class RegistrationsControllerV1 {
  Logger logger = LoggerFactory.getLogger(RegistrationsControllerV1.class);
  @Autowired WorkflowClient temporalClient;

  @Value("${spring.curriculum.task-queue}")
  String taskQueue;

  @PutMapping(
      value = "/{id}",
      consumes = {MediaType.APPLICATION_JSON_VALUE})
  //      produces = {MediaType.APPLICATION_JSON_VALUE})
  ResponseEntity<String> registrationsPut(
      @PathVariable String id, @RequestBody RegistrationsPut params) {
    // poor man's inspection to decide whether to update the entity or start a workflow
    // we could as easily check for WF existence first to decide which is best action to take
    final WorkflowOptions options =
        WorkflowOptions.newBuilder()
            .setTaskQueue(taskQueue)
            .setWorkflowId("users")
            .setRetryOptions(null)
            //            .setWorkflowIdReusePolicy(
            //                WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_ALLOW_DUPLICATE)
            .setWorkflowIdConflictPolicy(
                WorkflowIdConflictPolicy.WORKFLOW_ID_CONFLICT_POLICY_USE_EXISTING)
            .build();
    var start = StartUserRegistrationsRequest.newBuilder().setName("users").build();
    var registration =
        RegisterUserRequest.newBuilder().setId(id).setEmail(params.getEmail()).build();
    var workflowStub = temporalClient.newWorkflowStub(UserRegistrations.class, options);

    try {
      var handle =
          WorkflowClient.startUpdateWithStart(
              workflowStub::registerUser,
              registration,
              UpdateOptions.newBuilder()
                  .setUpdateId(id)
                  .setWaitForStage(WorkflowUpdateStage.ACCEPTED)
                  .build(),
              new WithStartWorkflowOperation<>(workflowStub::start, start));
      RegisterUserResponse response = (RegisterUserResponse) handle.getResult();
      return ResponseEntity.status(HttpStatus.ACCEPTED)
          .header("location", "/api/v1/registrations/" + id + "?token=" + response.getToken())
          .build();
    } catch (Exception e) {
      logger.error("Error starting workflow", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PutMapping(
      value = "/{id}/authorizations/{code}",
      consumes = {MediaType.APPLICATION_JSON_VALUE})
  //      produces = {MediaType.APPLICATION_JSON_VALUE})
  ResponseEntity<String> registrationsPut(@PathVariable String id, @PathVariable String code) {

    try {
      var stub = temporalClient.newWorkflowStub(UserRegistrations.class, "users");
      var update =
          temporalClient
              .newUntypedWorkflowStub("users")
              .getUpdateHandle(id, RegisterUserResponse.class);
      var registration = update.getResult(10, TimeUnit.SECONDS);
      var authorization =
          stub.authorizeUser(
              AuthorizeUserRequest.newBuilder()
                  .setCode(code)
                  .setToken(registration.getToken())
                  .setEmail(registration.getEmail())
                  .build());
      return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    } catch (Exception e) {
      logger.error("Error starting workflow", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GetMapping(
      value = "/{id}",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  ResponseEntity<String> registrationGet(@PathVariable String id) {
    var stub = temporalClient.newWorkflowStub(UserRegistrations.class, "users");
    var state = stub.getState();
    var maybeReg =
        state.getUserRegistrationsList().stream()
            .filter(r -> Objects.equals(id, r.getId()))
            .findFirst();
    if (maybeReg.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    var reg = maybeReg.get();
    var user =
        stub.getState().getUsersList().stream()
            .filter(r -> Objects.equals(r.getEmail(), reg.getEmail()))
            .findFirst();

    var res =
        RegistrationGet.newBuilder()
            .setId(id)
            .setEmail(reg.getEmail())
            .setToken(reg.getToken())
            .setUserId(user.map(User::getUserId).orElse(""))
            .build();
    try {
      return ResponseEntity.ok(JsonFormat.printer().print(res));

    } catch (Exception e) {
      logger.error("Error finding update result", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
