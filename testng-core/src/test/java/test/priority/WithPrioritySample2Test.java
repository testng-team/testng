package test.priority;

import org.testng.annotations.Test;

public class WithPrioritySample2Test {
  @Test(priority = -2)
  public void first() {}

  @Test(priority = -3)
  public void second() {}
}
