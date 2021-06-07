package test.priority;

import org.testng.annotations.Test;

public class WithPrioritySampleTest {
  @Test(priority = -2)
  public void first() {}

  @Test(priority = -1)
  public void second() {}
}
