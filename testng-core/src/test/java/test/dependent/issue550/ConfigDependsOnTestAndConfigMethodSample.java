package test.dependent.issue550;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ConfigDependsOnTestAndConfigMethodSample {

  @BeforeClass
  public void beforeClass() {}

  @BeforeClass
  public void anotherBeforeClass() {}

  @BeforeMethod(dependsOnMethods = {"testMethod", "anotherBeforeMethod"})
  public void beforeMethod() {}

  @BeforeMethod
  public void anotherBeforeMethod() {}

  @Test
  public void testMethod() {}
}
