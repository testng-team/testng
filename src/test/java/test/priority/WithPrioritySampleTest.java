package test.priority;

import org.testng.annotations.Test;


public class WithPrioritySampleTest extends BaseSample {
  @Test(priority = -2)
  public void first() {
    add("first");
  }

  @Test(priority = -1)
  public void second() {
    add("second");
  }

}
