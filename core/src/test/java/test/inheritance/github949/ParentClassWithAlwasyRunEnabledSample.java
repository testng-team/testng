package test.inheritance.github949;

import org.testng.annotations.Test;

public class ParentClassWithAlwasyRunEnabledSample extends CommonBaseClass {
  @Test
  public void independent() {
    logMessage();
  }

  @Test(dependsOnMethods = "independent", alwaysRun = true)
  public void dependent() {
    logMessage();
  }
}
