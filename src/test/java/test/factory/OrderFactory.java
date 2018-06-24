package test.factory;

import org.testng.annotations.Factory;

import java.util.ArrayList;
import java.util.List;

public class OrderFactory {

  @Factory
  public static Object[] testF() {
    List<OrderSample> result = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      result.add(new OrderSample(i));
    }
    return result.toArray();
  }
}
