package test.priority;

import org.testng.annotations.Test;

public class WithoutPrioritySampleTest extends BaseSample {

  @Test
  public void first() {
    add("first");
  }

  @Test
  public void second() {
    add("second");
  }
}
