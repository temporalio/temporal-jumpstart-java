# Testing Workflows

This presumes `SpringBootTest` when discussing _how_ to inject Mocks or Stubs but the
_principles_ apply regardless of whether Spring is used or not.

**Note**
The techniques described support both explicit and autoconfigure Temporal SpringBoot integrations.

## Mocked Activities

Given the following spring configuration in `application-test.yaml`
```yaml
  temporal:
    test-server:
      enabled: true
    workers:
      - task-queue: onboardings
        # name: your-worker-name # unique name of the Worker. If not specified, Task Queue is used as the Worker name.
        workflow-classes:
          - io.temporal.onboardings.domain.orchestrations.EntityOnboardingImpl
        activity-beans:
          - integrations-handlers
          - notifications-handlers
    namespace: default # https://docs.temporal.io/cloud/#temporal-cloud-namespace-id
    connection:
      target: 127.0.0.1:7233

```


##### _Simple Case: no return arguments example_
Overriding registered Activity implementations can be as simple as providing a `MockBean` for the dependency interface.

```java
@SpringBootTest(
    classes = {
      EntityOnboardingTest.Configuration.class,
    })
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@EnableAutoConfiguration()
@DirtiesContext
// use config from application-test.yaml
@ActiveProfiles("test")
// register services from domain
@Import(DomainConfig.class)
public class EntityOnboardingTest {
  @Autowired ConfigurableApplicationContext applicationContext;

  @Autowired TestWorkflowEnvironment testWorkflowEnvironment;

  @Autowired WorkflowClient workflowClient;

  @MockBean CrmListener listener;

  // this is an Activity definition
  @MockBean IntegrationsHandlers integrationsHandlers;

  // pass in our taskqueue from our configuration
  @Value("${spring.temporal.workers[0].task-queue}")
  String taskQueue;

  @AfterEach
  public void after() {
    testWorkflowEnvironment.close();
  }
  @BeforeEach
  void beforeEach() { applicationContext.start(); }
  }
  @Test
  public void doTest() {
    //
  }
  @ComponentScan
  public static class Configuration {}
```

##### _Advanced Case: returned arguments example_

Often you need to return a value to the Workflow to test particular code paths though.
In this case, you can create a `Bean` that returns a `MockBean` and attaches behavior
you want to test for.

```java
import io.temporal.onboardings.domain.integrations.IntegrationsHandlers;
import io.temporal.onboardings.domain.notifications.NotificationsHandlers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@SpringBootTest(
        classes = {
                EntityOnboardingTest.Configuration.class,
        })
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@EnableAutoConfiguration()
@DirtiesContext
// use config from application-test.yaml
@ActiveProfiles("test")
// register services from domain
@Import(DomainConfig.class)
public class EntityOnboardingTest {
    @Autowired
    ConfigurableApplicationContext applicationContext;

    @Autowired
    TestWorkflowEnvironment testWorkflowEnvironment;

    @Autowired
    WorkflowClient workflowClient;

    @MockBean
    CrmListener listener;

    // this is an Activity definition
    @Autowired
    IntegrationsHandlers integrationsHandlers;

    // pass in our taskqueue from our configuration
    @Value("${spring.temporal.workers[0].task-queue}")
    String taskQueue;

    @AfterEach
    public void after() {
        testWorkflowEnvironment.close();
    }

    @BeforeEach
    void beforeEach() {
        applicationContext.start();
    }
}

@Test
public void doTest() {
    //
}

@ComponentScan
public static class Configuration {
    @MockBean
    private IntegrationsHandlers integrationsHandlers;

    // you may not need the @Primary or `name` value for the dependency
    // to override the registration order for your Tests
    @Primary
    @Bean(name = "integrations-handlers")
    public IntegrationsHandlers getIntegrationsHandlers() {
        Mockito.when(integrationsHandlers.someMethodThatReturnsAnArg()).thenReturn("foo");
        return integrationsHandlers;
    }
}
```

