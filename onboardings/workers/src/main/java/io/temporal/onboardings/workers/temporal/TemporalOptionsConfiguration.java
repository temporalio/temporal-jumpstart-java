package io.temporal.onboardings.workers.temporal;

import io.temporal.spring.boot.WorkerOptionsCustomizer;
import io.temporal.worker.WorkerOptions;
import io.temporal.worker.tuning.*;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
public class TemporalOptionsConfiguration {
  Logger logger = LoggerFactory.getLogger(TemporalOptionsConfiguration.class);

  @Bean
  public WorkerOptionsCustomizer workerCustomizer() {
    return new WorkerOptionsCustomizer() {
      @Nonnull
      @Override
      public WorkerOptions.Builder customize(
          @Nonnull WorkerOptions.Builder optionsBuilder,
          @Nonnull String workerName,
          @Nonnull String taskQueue) {
        logger.info("Adding ResourceBasedTuner to WorkerOptions");
        optionsBuilder.setWorkerTuner(
            ResourceBasedTuner.newBuilder()
                .setControllerOptions(ResourceBasedControllerOptions.newBuilder(0.75, 0.75).build())
                .build());
        return optionsBuilder;
      }
    };
  }
}
