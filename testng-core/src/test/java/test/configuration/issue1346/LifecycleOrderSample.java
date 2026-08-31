package test.configuration.issue1346;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/** One test method in one group, and one method of every configuration kind around it. */
public class LifecycleOrderSample {

  @BeforeSuite
  public void beforeSuite() {}

  @BeforeTest
  public void beforeTest() {}

  @BeforeGroups("g1")
  public void beforeGroups() {}

  @BeforeClass
  public void beforeClass() {}

  @BeforeMethod
  public void beforeMethod() {}

  @Test(groups = "g1")
  public void test() {}

  @AfterMethod
  public void afterMethod() {}

  @AfterClass
  public void afterClass() {}

  @AfterGroups("g1")
  public void afterGroups() {}

  @AfterTest
  public void afterTest() {}

  @AfterSuite
  public void afterSuite() {}
}
