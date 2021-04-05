package test.factory;

import org.testng.ITest;
import org.testng.annotations.Test;

public class FactoryBaseSample implements ITest {

  private final int value;

  public FactoryBaseSample() {
    this(0);
  }

  public FactoryBaseSample(int value) {
    this.value = value;
  }

  @Test
  public void f() {}

  @Override
  public String getTestName() {
    return "FactoryBaseSample{" + value + "}";
  }
}
