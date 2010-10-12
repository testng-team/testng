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
import org.testng.annotations.Configuration;
import org.testng.annotations.DataProvider;
import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Factory;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

@Test(enabled = true, groups = {"group1", "group2"},
    alwaysRun = true, parameters = {"param1", "param2"},
    dependsOnGroups = {"dg1", "dg2"}, dependsOnMethods = {"dm1", "dm2"},
    timeOut = 42, invocationCount = 43, successPercentage = 44,
    threadPoolSize = 3,
    dataProvider = "dp", description = "Class level description")
public class MTest1 {

  @Test(enabled = true, groups = {"group5", "group6"},
      alwaysRun = true, parameters = {"param5", "param6"},
      dependsOnGroups = {"dg5", "dg6"}, dependsOnMethods = {"dm5", "dm6"},
      timeOut = 242, invocationCount = 243, successPercentage = 62,
      dataProvider = "dp3", description = "Constructor description",
      expectedExceptions = NumberFormatException.class)
  public MTest1() {}

  @Test(enabled = true, groups = {"group3", "group4"},
      alwaysRun = true, parameters = {"param3", "param4"},
      dependsOnGroups = {"dg3", "dg4"}, dependsOnMethods = {"dm3", "dm4"},
      timeOut = 142, invocationCount = 143, successPercentage = 61,
      dataProvider = "dp2", description = "Method description",
      expectedExceptions = NullPointerException.class)
  public void f() {}

  @Configuration(beforeSuite = true, beforeTestMethod = true,
      beforeTest = true, beforeTestClass = true,
      beforeGroups = { "b1", "b2"})
  public void before() {}

  @Configuration(afterSuite = true, afterTestMethod = true,
      afterTest = true, afterTestClass = true,
      afterGroups = {"a1", "a2"})
  public void after() {}

  @Configuration(parameters = {"oparam1", "oparam2"},
      enabled = false, groups = {"ogroup1", "ogroup2"},
      dependsOnGroups = {"odg1","odg2"},
      dependsOnMethods = {"odm1", "odm2"}, alwaysRun = true,
      inheritGroups = false,
      description = "beforeSuite description")
   @DataProvider(name = "dp4")
   @ExpectedExceptions({MTest1.class, MTest2.class })
  public void otherConfigurations() {}

  @Factory(parameters = {"pf1", "pf2"})
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
