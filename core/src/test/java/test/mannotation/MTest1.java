package test.mannotation;

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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

@Test(enabled = true, groups = {"group1", "group2"},
    alwaysRun = true,
    dependsOnGroups = {"dg1", "dg2"}, dependsOnMethods = {"dm1", "dm2"},
    timeOut = 42, invocationCount = 43, successPercentage = 44,
    threadPoolSize = 3,
    dataProvider = "dp", description = "Class level description")
public class MTest1 {

  public MTest1() {}

  @Test(enabled = true, groups = {"group3", "group4"},
      alwaysRun = true,
      dependsOnGroups = {"dg3", "dg4"}, dependsOnMethods = {"dm3", "dm4"},
      timeOut = 142, invocationCount = 143, successPercentage = 61,
      dataProvider = "dp2", description = "Method description",
      expectedExceptions = NullPointerException.class)
  public void f() {}

  @BeforeSuite
  @BeforeTest
  @BeforeGroups({ "b1", "b2"})
  @BeforeClass
  @BeforeMethod
  public void before() {}

  @AfterSuite
  @AfterTest
  @AfterGroups({"a1", "a2"})
  @AfterClass
  @AfterMethod
  public void after() {}

  @Test(groups = {"ogroup1", "ogroup2"}, dependsOnGroups = {"odg1", "odg2"}, dependsOnMethods = {"odm1", "odm2"},
          description = "beforeSuite description", enabled = false, alwaysRun = true,
          expectedExceptions = {MTest1.class, MTest2.class})
  @DataProvider(name = "dp4")
  public Object[][] otherConfigurations() {
    return null;
  }

  @Factory
  public void factory() {}

  @Parameters({"pp1", "pp2", "pp3"})
  public void parameters() {}

  @BeforeSuite
  @BeforeTest
  @BeforeGroups
  @BeforeClass
  @BeforeMethod
  public void newBefore() {}

  @AfterSuite
  @AfterTest
  @AfterGroups
  @AfterClass
  @AfterMethod
  public void newAfter() {}

}
