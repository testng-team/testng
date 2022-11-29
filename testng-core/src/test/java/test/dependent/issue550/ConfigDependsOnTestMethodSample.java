package test.dependent.issue550;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ConfigDependsOnTestMethodSample {

  @BeforeMethod(dependsOnMethods = "testMethod")
  public void beforeMethod() {}

  @Test
  public void testMethod() {}
}
