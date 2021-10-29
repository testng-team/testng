package test.configuration.issue2664;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class ConfigurationMethodWithGroupDependencySample2
    extends ConfigurationMethodWithGroupDependencyBase1 {

  @BeforeSuite(groups = {"g1"})
  public void beforeSuite3() {}

  @BeforeSuite(
      groups = {"g2"},
      dependsOnGroups = "g1")
  public void beforeSuite2() {}

  @BeforeSuite(
      groups = {"g2"},
      dependsOnGroups = "g1")
  public void beforeSuite1() {}

  @BeforeClass(groups = {"g1"})
  public void beforeClass3() {}

  @BeforeClass(
      groups = {"g2"},
      dependsOnGroups = "g1")
  public void beforeClass2() {}

  @BeforeClass(
      groups = {"g2"},
      dependsOnGroups = "g1")
  public void beforeClass1() {}

  @BeforeTest(groups = {"g1"})
  public void beforeTest3() {}

  @BeforeTest(
      groups = {"g2"},
      dependsOnGroups = "g1")
  public void beforeTest2() {}

  @BeforeTest(
      groups = {"g2"},
      dependsOnGroups = "g1")
  public void beforeTest1() {}

  @BeforeMethod(groups = {"g1"})
  public void beforeMethod3() {}

  @BeforeMethod(
      groups = {"g2"},
      dependsOnGroups = "g1")
  public void beforeMethod2() {}

  @BeforeMethod(
      groups = {"g2"},
      dependsOnGroups = "g1")
  public void beforeMethod1() {}

  @Test(groups = {"g1"})
  public void test3() {}

  @Test(
      groups = {"g2"},
      dependsOnGroups = "g1",
      priority = 0)
  public void test2() {}

  @Test(
      groups = {"g3"},
      dependsOnGroups = "g1",
      priority = 1)
  public void test1() {}

  @AfterMethod(groups = {"g1"})
  public void afterMethod3() {}

  @AfterMethod(
      groups = {"g2"},
      dependsOnGroups = "g1")
  public void afterMethod2() {}

  @AfterMethod(
      groups = {"g2"},
      dependsOnGroups = "g1")
  public void afterMethod1() {}

  @AfterClass(groups = {"g1"})
  public void afterClass3() {}

  @AfterClass(
      groups = {"g2"},
      dependsOnGroups = "g1")
  public void afterClass2() {}

  @AfterClass(
      groups = {"g2"},
      dependsOnGroups = "g1")
  public void afterClass1() {}

  @AfterTest(groups = {"g1"})
  public void afterTest3() {}

  @AfterTest(
      groups = {"g2"},
      dependsOnGroups = "g1")
  public void afterTest2() {}

  @AfterTest(
      groups = {"g2"},
      dependsOnGroups = "g1")
  public void afterTest1() {}

  @AfterSuite(groups = {"g1"})
  public void afterSuite3() {}

  @AfterSuite(
      groups = {"g2"},
      dependsOnGroups = "g1")
  public void afterSuite2() {}

  @AfterSuite(
      groups = {"g2"},
      dependsOnGroups = "g1")
  public void afterSuite1() {}
}
