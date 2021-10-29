package test.configuration.issue2664;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class ConfigurationMethodWithGroupDependencyBase1 {

  @BeforeSuite(groups = {"g0"})
  public void beforeSuiteBase() {}

  @BeforeSuite(dependsOnGroups = "g2")
  public void beforeSuiteBase2() {}

  @BeforeClass(groups = {"g0"})
  public void beforeClassBase() {}

  @BeforeClass(dependsOnGroups = "g2")
  public void beforeClassBase2() {}

  @BeforeTest(groups = {"g0"})
  public void beforeTestBase() {}

  @BeforeTest(dependsOnGroups = "g2")
  public void beforeTestBase2() {}

  @BeforeMethod(groups = {"g0"})
  public void beforeMethodBase() {}

  @BeforeMethod(dependsOnGroups = "g2")
  public void beforeMethodBase2() {}

  @Test(groups = {"g0"})
  public void testBase() {}
}
