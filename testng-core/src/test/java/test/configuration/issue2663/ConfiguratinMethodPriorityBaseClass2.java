package test.configuration.issue2663;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class ConfiguratinMethodPriorityBaseClass2 extends ConfiguratinMethodPriorityBaseClass1 {

  @BeforeSuite(
      groups = {"g2"},
      dependsOnGroups = "g1",
      priority = 0)
  public void beforeSuite2() {
    print();
  }

  @BeforeSuite(
      groups = {"g2"},
      dependsOnGroups = "g1",
      priority = 1)
  public void beforeSuite1() {
    print();
  }

  @BeforeClass(
      groups = {"g2"},
      dependsOnGroups = "g1",
      priority = 0)
  public void beforeClass2() {
    print();
  }

  @BeforeClass(
      groups = {"g2"},
      dependsOnGroups = "g1",
      priority = 1)
  public void beforeClass1() {
    print();
  }

  @BeforeTest(
      groups = {"g2"},
      dependsOnGroups = "g1",
      priority = 0)
  public void beforeTest2() {
    print();
  }

  @BeforeTest(
      groups = {"g2"},
      dependsOnGroups = "g1",
      priority = 1)
  public void beforeTest1() {
    print();
  }

  @BeforeMethod(
      groups = {"g2"},
      dependsOnGroups = "g1",
      priority = 0)
  public void beforeMethod2() {
    print();
  }

  @BeforeMethod(
      groups = {"g2"},
      dependsOnGroups = "g1",
      priority = 1)
  public void beforeMethod1() {
    print();
  }

  @Test(
      groups = {"g2"},
      dependsOnGroups = "g1",
      priority = 0)
  public void test2() {
    print();
  }
}
