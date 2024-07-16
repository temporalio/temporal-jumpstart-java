package io.temporal.app.domain.orchestrations;

import static java.util.Map.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

import io.temporal.app.domain.DomainConfig;
import io.temporal.app.domain.products.ProductHandlers;
import io.temporal.app.messages.commands.CompleteProductFulfillmentRequest;
import io.temporal.app.messages.commands.FulfillAccommodationRequest;
import io.temporal.app.messages.commands.FulfillTaxiRequest;
import io.temporal.app.messages.commands.ProductFulfillmentRequest;
import io.temporal.app.messages.orchestrations.SubmitOrderRequest;
import io.temporal.app.messages.queries.GetProductFulfillmentConfigurationRequest;
import io.temporal.app.messages.queries.GetProductFulfillmentConfigurationResponse;
import io.temporal.app.messages.values.FulfillmentStatus;
import io.temporal.app.messages.values.ProductFulfillmentConfiguration;
import io.temporal.app.messages.values.ProductType;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowFailedException;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(
    classes = {
      OrderImplTest.Configuration.class,
    })
// @ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@EnableAutoConfiguration()
@DirtiesContext
@Import(DomainConfig.class)
public class OrderImplTest {
  @Autowired ConfigurableApplicationContext applicationContext;

  @Autowired TestWorkflowEnvironment testWorkflowEnvironment;

  @Autowired WorkflowClient workflowClient;

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

  @Autowired
  @Qualifier("product-handlers")
  ProductHandlers productHandlers;

  private static Map<ProductType, ProductFulfillmentConfiguration> testProductConfig =
      Map.ofEntries(
          entry(ProductType.TAXI, new ProductFulfillmentConfiguration(ProductType.TAXI, 30)),
          entry(
              ProductType.ACCOMMODATION,
              new ProductFulfillmentConfiguration(ProductType.ACCOMMODATION, 40)),
          entry(ProductType.FLIGHT, new ProductFulfillmentConfiguration(ProductType.FLIGHT, 50)));

  @Test
  public void givenValidArgs_itShouldExecuteWorkflow() {
    String wfId = UUID.randomUUID().toString();
    var dt = Instant.parse("2024-11-30T18:00:00.00Z");
    var args =
        new SubmitOrderRequest(
            wfId,
            UUID.randomUUID().toString(),
            new FulfillTaxiRequest(
                ProductType.TAXI, UUID.randomUUID().toString(), "Stockholm Taxi", Date.from(dt)));

    Order sut =
        workflowClient.newWorkflowStub(
            Order.class,
            WorkflowOptions.newBuilder().setWorkflowId(wfId).setTaskQueue(taskQueue).build());
    Assertions.assertDoesNotThrow(
        () -> {
          sut.execute(args);
        });
  }

  @Test
  public void givenInvalidArgs_itShouldNotExecuteWorkflow() {
    String wfId = UUID.randomUUID().toString();
    var args = new SubmitOrderRequest(wfId, UUID.randomUUID().toString()); // missing product

    Order sut =
        workflowClient.newWorkflowStub(
            Order.class,
            WorkflowOptions.newBuilder().setWorkflowId(wfId).setTaskQueue(taskQueue).build());
    Assertions.assertThrows(
        WorkflowFailedException.class,
        () -> {
          sut.execute(args);
          //                    var run = WorkflowClient.execute(() -> sut.execute(args));
          //                    run.get();
        });
  }

  @Test
  public void givenInvalidArgs_itShouldNotExecuteWorkflowAsync() {
    String wfId = UUID.randomUUID().toString();
    var args = new SubmitOrderRequest(wfId, UUID.randomUUID().toString()); // missing product

    Order sut =
        workflowClient.newWorkflowStub(
            Order.class,
            WorkflowOptions.newBuilder().setWorkflowId(wfId).setTaskQueue(taskQueue).build());
    var e =
        Assertions.assertThrows(
            ExecutionException.class,
            () -> {
              var run = WorkflowClient.execute(() -> sut.execute(args));
              run.get();
            });
  }

  @Test
  public void givenMultiProductsExceptFlightAllSucceed_whenSubmitted_itShouldCompleteOrder() {
    String wfId = UUID.randomUUID().toString();
    var dt = Instant.parse("2024-11-30T18:00:00.00Z");
    var taxi =
        new FulfillTaxiRequest(
            ProductType.TAXI, UUID.randomUUID().toString(), "Stockholm Taxi", Date.from(dt));
    var accommodation =
        new FulfillAccommodationRequest(
            ProductType.ACCOMMODATION,
            UUID.randomUUID().toString(),
            "Comfort Arlanda",
            Date.from(dt),
            Date.from(dt.plus(Duration.ofDays(1))));
    //    var flight =
    //        new FulfillFlightRequest(
    //            ProductType.FLIGHT, UUID.randomUUID().toString(), "Lufthansa", "SB128");

    var args = new SubmitOrderRequest(wfId, UUID.randomUUID().toString(), taxi, accommodation);
    var productTypes =
        Arrays.stream(args.fulfillmentRequests())
            .map(ProductFulfillmentRequest::productType)
            .toList();
    Order sut =
        workflowClient.newWorkflowStub(
            Order.class,
            WorkflowOptions.newBuilder().setWorkflowId(wfId).setTaskQueue(taskQueue).build());
    var run = WorkflowClient.start(sut::execute, args);
    testWorkflowEnvironment.sleep(Duration.ofSeconds(10));
    sut.completeProductFulfillment(
        new CompleteProductFulfillmentRequest(
            taxi.productType(), taxi.id(), FulfillmentStatus.SUCCEEDED));
    testWorkflowEnvironment.sleep(Duration.ofSeconds(3));
    sut.completeProductFulfillment(
        new CompleteProductFulfillmentRequest(
            accommodation.productType(), accommodation.id(), FulfillmentStatus.SUCCEEDED));
    sut.execute(args);
    verify(productHandlers)
        .getFulfillmentConfiguration(
            argThat(
                req ->
                    req.productTypes().length == productTypes.size()
                        && Arrays.asList(req.productTypes()).containsAll(productTypes)));
    verify(productHandlers).fulfillAccommodation(accommodation);
    verify(productHandlers).fulfillTaxi(taxi);
    var state = sut.getOrderState();
    Assertions.assertEquals(2, state.getCompletions().size());
  }

  @ComponentScan
  public static class Configuration {
    @MockBean(name = "product-handlers")
    private ProductHandlers productHandlers;

    @Bean
    public ProductHandlers getProductHandlersBean() {

      Mockito.when(
              productHandlers.getFulfillmentConfiguration(
                  any(GetProductFulfillmentConfigurationRequest.class)))
          .thenAnswer(
              invocation -> {
                var arg =
                    invocation.getArgument(0, GetProductFulfillmentConfigurationRequest.class);
                var list = new ArrayList<ProductFulfillmentConfiguration>();
                for (ProductType productType : arg.productTypes()) {
                  list.add(
                      new ProductFulfillmentConfiguration(
                          productType, testProductConfig.get(productType).timeoutSeconds()));
                }
                return new GetProductFulfillmentConfigurationResponse(list);
              });

      return productHandlers;
    }
  }
}
