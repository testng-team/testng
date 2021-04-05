package test.inheritance.github949;

import org.testng.annotations.Test;

public class ParentClassSample extends CommonBaseClass {
  @Test
  public void independent() {
    logMessage();
  }

  @Test(dependsOnMethods = "independent")
  public void dependent() {
    logMessage();
  }
}
