package test.priority;

import org.testng.annotations.Test;

public class WithPriorityAndDependsMethodsSample {

  @Test
  public void first() {}

  @Test(dependsOnMethods = {"first"})
  public void second() {}

  @Test(priority = 1)
  public void third() {}
}
