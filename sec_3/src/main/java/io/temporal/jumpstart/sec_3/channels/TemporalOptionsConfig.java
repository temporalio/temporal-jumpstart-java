package io.temporal.jumpstart.sec_3.channels;

import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.spring.boot.TemporalOptionsCustomizer;
import javax.annotation.Nonnull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TemporalOptionsConfig {

  // WorkflowServiceStubsOptions customization
  // This is more advanced usage, where one can do things like interceptors or other rules on
  // the underlying gRPC API for the SDK
  @Bean
  public TemporalOptionsCustomizer<WorkflowServiceStubsOptions.Builder>
      customServiceStubsOptions() {
    return new TemporalOptionsCustomizer<>() {
      @Nonnull
      @Override
      public WorkflowServiceStubsOptions.Builder customize(
          @Nonnull WorkflowServiceStubsOptions.Builder optionsBuilder) {
        // set options on optionsBuilder as needed
        // ...
        return optionsBuilder;
      }
    };
  }

  // WorkflowClientOption customization
  // One might tune the client being used with things like using a Data Converter, a dynamic
  // Namespace name (instead of from the resources config)
  @Bean
  public TemporalOptionsCustomizer<WorkflowClientOptions.Builder> customClientOptions() {
    return new TemporalOptionsCustomizer<>() {
      @Nonnull
      @Override
      public WorkflowClientOptions.Builder customize(
          @Nonnull WorkflowClientOptions.Builder optionsBuilder) {
        // set options on optionsBuilder as needed
        // ...
        return optionsBuilder;
      }
    };
  }
}
