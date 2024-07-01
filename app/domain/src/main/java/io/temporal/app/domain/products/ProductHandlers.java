package io.temporal.app.domain.products;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface ProductHandlers {
  @ActivityMethod
  void fulfillProduct();
}
