package io.temporal.jumpstart.sec_3.channels;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class APIController {
  @Autowired WorkflowClient temporalClient;

  @PostMapping(
      value = "/workflows",
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.TEXT_HTML_VALUE})
  ResponseEntity post(@RequestBody WorkflowParamsRequest params) {
    WorkflowOptions workflowOptions =
        WorkflowOptions.newBuilder()
            .setTaskQueue("myTaskQueue")
            .setWorkflowId(params.getBusinessId())
            // you will rarely ever want to set this on WorkflowOptions
            // .setRetryOptions()
            .build();
    WorkflowStub workflowStub =
        temporalClient.newUntypedWorkflowStub("WorkflowTypeTBD", workflowOptions);
    // this starts the workflow but does not block
    workflowStub.start();

    // this blocks while waiting for the result from the workflow
    String result = workflowStub.getResult(String.class);

    // bypass thymeleaf, don't return template name just result
    return new ResponseEntity<>("\"" + result + "\"", HttpStatus.OK);
  }
}
