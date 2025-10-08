package io.temporal.inframanager.domain;

import io.temporal.inframanager.domain.interceptors.NoopInterceptor;
import io.temporal.spring.boot.TemporalOptionsCustomizer;
import io.temporal.worker.WorkerFactoryOptions;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
public class TemporalOptionsConfig {
  Logger logger = LoggerFactory.getLogger(TemporalOptionsConfig.class);
  @Autowired private NoopInterceptor noopInterceptor;

  @Value("${spring.application.enable-interceptors}")
  private Boolean interceptorsEnabled;

  @Bean
  public TemporalOptionsCustomizer<WorkerFactoryOptions.Builder> customWorkerFactoryOptions() {
    return new TemporalOptionsCustomizer<>() {
      @Nonnull
      @Override
      public WorkerFactoryOptions.Builder customize(
          @Nonnull WorkerFactoryOptions.Builder optionsBuilder) {
        if (interceptorsEnabled) {
          optionsBuilder.setWorkerInterceptors(noopInterceptor);
        }
        return optionsBuilder;
      }
    };
  }
}
