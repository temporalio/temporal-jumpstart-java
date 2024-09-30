package io.temporal.app.domain.products;

import org.springframework.stereotype.Component;

@Component("product-handlers")
public class HandlersImpl implements ProductHandlers {
  @Override
  public void fulfillProduct() {

    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
