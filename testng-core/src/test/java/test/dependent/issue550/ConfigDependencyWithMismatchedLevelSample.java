package test.dependent.issue550;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ConfigDependencyWithMismatchedLevelSample {

  @BeforeClass
  public void beforeClass() {}

  @BeforeMethod(dependsOnMethods = "beforeClass")
  public void beforeMethod() {}

  @Test
  public void testMethod() {}
}
