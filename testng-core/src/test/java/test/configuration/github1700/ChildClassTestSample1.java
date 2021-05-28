package test.configuration.github1700;

import org.testng.annotations.Test;

public class ChildClassTestSample1 extends BaseClassSample {
  @Test
  public void test1() {
    messages.add(getClass().getCanonicalName() + ".test1()");
  }
}
