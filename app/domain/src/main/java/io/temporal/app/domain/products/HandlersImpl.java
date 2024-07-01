package io.temporal.app.domain.products;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component("product-handlers")
public class HandlersImpl implements ProductHandlers {
  @Override
  public void fulfillProduct() {

      try {
          Thread.sleep(Duration.ofSeconds(2));
      } catch (InterruptedException e) {
          throw new RuntimeException(e);
      }

  }
}
