package io.temporal.app.domain.products;

import java.time.Duration;
import org.springframework.stereotype.Component;

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
