package test.dependent;

import org.testng.annotations.Test;

public class SampleDependentMethods6 {
  @Test(dependsOnMethods = {"step2"})
  public void step1() {}

  @Test(dependsOnMethods = {"step1"})
  public void step2() {}
}
