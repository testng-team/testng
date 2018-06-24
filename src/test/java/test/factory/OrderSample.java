package test.factory;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OrderSample {

  final int value;

  public OrderSample(int j) {
    value = j;
  }

  @BeforeClass(groups = {"s1ds"})
  public void setup() {}

  @Test(groups = {"s1ds"})
  public void methodC1() {}

  @AfterClass(groups = {"s1ds"})
  public void cleanup() {}

  @Override
  public String toString() {
    return "[OrderSample " + value + "]";
  }

  public int getValue() {
    return value;
  }
}
