package io.temporal.fsi.web.controllers;

import com.google.protobuf.util.JsonFormat;
import io.temporal.api.enums.v1.WorkflowIdConflictPolicy;
import io.temporal.api.enums.v1.WorkflowIdReusePolicy;
import io.temporal.client.*;
import io.temporal.fsi.api.applications.v1.*;
import io.temporal.fsi.api.web.v1.AccountGet;
import io.temporal.fsi.api.web.v1.AccountsPut;
import io.temporal.fsi.domain.accounts.workflows.wealthmanagement.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(
    origins = "*",
    allowedHeaders = "*",
    methods = {RequestMethod.PUT, RequestMethod.GET})
@RequestMapping("/api/v1/accounts")
public class AccountsControllerV1 {
  Logger logger = LoggerFactory.getLogger(AccountsControllerV1.class);
  @Autowired WorkflowClient temporalClient;

  @Value("${spring.curriculum.task-queue}")
  String taskQueue;

  @PutMapping(
      value = "/{id}",
      consumes = {MediaType.APPLICATION_JSON_VALUE})
  //      produces = {MediaType.APPLICATION_JSON_VALUE})
  ResponseEntity<String> applicationsPut(@PathVariable String id, @RequestBody AccountsPut params) {

    if (params.hasBirthdate() && params.hasSsn() && params.hasName()) {
      try {
        var handle = temporalClient.newWorkflowStub(Account.class, id);
        var state =
            handle.matchClient(
                MatchClientRequest.newBuilder()
                    .setBirthdate(params.getBirthdate())
                    .setSsn(params.getSsn())
                    .setName(params.getName())
                    .build());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
            .header("location", "/api/v1/accounts/" + id)
            .build();
      } catch (Exception e) {
        logger.error("Error updating workflow", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
      }
    }
    final WorkflowOptions options =
        WorkflowOptions.newBuilder()
            .setTaskQueue(taskQueue)
            .setWorkflowId(id)
            .setRetryOptions(null)
            .setWorkflowIdReusePolicy(
                WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_ALLOW_DUPLICATE_FAILED_ONLY)
            .setWorkflowIdConflictPolicy(WorkflowIdConflictPolicy.WORKFLOW_ID_CONFLICT_POLICY_FAIL)
            .build();
    var args = OpenWealthManagementAccountRequest.newBuilder().setUserId(id).build();

    var workflowStub = temporalClient.newWorkflowStub(Account.class, options);

    try {
      var handle = WorkflowClient.start(workflowStub::start, args);

      return ResponseEntity.status(HttpStatus.ACCEPTED)
          .header("location", "/api/v1/accounts/" + id)
          .build();
    } catch (Exception e) {
      logger.error("Error starting workflow", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GetMapping(
      value = "/{id}",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  ResponseEntity<String> applicationGet(@PathVariable String id) {
    try {
      var stub = temporalClient.newWorkflowStub(Account.class, id);
      var state = stub.getState();

      var resp =
          AccountGet.newBuilder()
              .setUserId(state.getUserId())
              .setClientId(state.getClientId())
              .setName(state.getName())
              .build();

      return ResponseEntity.ok(JsonFormat.printer().print(resp));

    } catch (Exception e) {
      logger.error("Error getting application", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
