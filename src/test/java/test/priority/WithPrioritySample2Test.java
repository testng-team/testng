package test.priority;

import org.testng.annotations.Test;


public class WithPrioritySample2Test extends BaseSample {
  @Test(priority = -2)
  public void first() {
    add("first");
  }

  @Test(priority = -3)
  public void second() {
    add("second");
  }

}
