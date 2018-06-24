package test.configuration.github1700;

import org.testng.annotations.Test;

public class ChildClassTestSample2 extends BaseClassSample {
  @Test
  public void test2() {
    messages.add(getClass().getCanonicalName() + ".test2()");
  }
}
