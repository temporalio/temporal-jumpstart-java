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

package io.temporal.onboardings.domain.integrations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.temporal.failure.ActivityFailure;
import io.temporal.failure.ApplicationFailure;
import io.temporal.onboardings.domain.DomainConfig;
import io.temporal.onboardings.domain.clients.crm.CrmClient;
import io.temporal.onboardings.domain.messages.commands.RegisterCrmEntityRequest;
import io.temporal.onboardings.domain.messages.orchestrations.Errors;
import io.temporal.testing.TestActivityEnvironment;
import io.temporal.testing.TestEnvironmentOptions;
import java.net.ConnectException;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;

@SpringBootTest(
    classes = {
      IntegrationsHandlersImplTest.Configuration.class,
    })
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@EnableAutoConfiguration()
@DirtiesContext
@Import(DomainConfig.class)
public class IntegrationsHandlersImplTest {
  @Autowired ConfigurableApplicationContext applicationContext;

  //  @Autowired TestWorkflowEnvironment testWorkflowEnvironment;

  //  @Autowired WorkflowClient workflowClient;

  @Autowired IntegrationsHandlers sut;

  @MockBean CrmClient crmClient;

  @Value("${spring.temporal.workers[0].task-queue}")
  String taskQueue;

  TestActivityEnvironment testActivityEnvironment;

  @BeforeEach
  void beforeEach() {
    testActivityEnvironment =
        TestActivityEnvironment.newInstance(TestEnvironmentOptions.newBuilder().build());
    testActivityEnvironment.registerActivitiesImplementations(sut);

    applicationContext.start();
  }

  @Test
  public void registerCrmEntity_givenConnectivityProblem_shouldThrowServiceUnrecoverable() {
    var cmd =
        new RegisterCrmEntityRequest(UUID.randomUUID().toString(), UUID.randomUUID().toString());

    when(crmClient.getCustomerById(cmd.id()))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
    try {
      Mockito.doThrow(ConnectException.class).when(crmClient).registerCustomer(any(), any());
    } catch (ConnectException e) {
      throw new RuntimeException(e);
    }
    var stub = testActivityEnvironment.newActivityStub(IntegrationsHandlers.class);

    var e =
        Assertions.assertThrows(
            ActivityFailure.class,
            () -> {
              stub.registerCrmEntity(cmd);
            });
    var ae = Assertions.assertInstanceOf(ApplicationFailure.class, e.getCause());
    Assertions.assertEquals(Errors.SERVICE_UNRECOVERABLE.name(), ae.getType());
  }

  @ComponentScan
  public static class Configuration {}
}
