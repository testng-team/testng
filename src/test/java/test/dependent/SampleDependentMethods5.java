package test.dependent;

import org.testng.annotations.Test;

public class SampleDependentMethods5 {

  @Test
  public void step1() {}

  @Test(dependsOnMethods = {"step1", "blablabla"})
  public void step2() {}
}
