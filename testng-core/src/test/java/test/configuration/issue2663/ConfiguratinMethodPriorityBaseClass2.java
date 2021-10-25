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
  public void s2() {
    System.out.println("s2");
  }

  @BeforeSuite(
      groups = {"g2"},
      dependsOnGroups = "g1",
      priority = 1)
  public void s1() {
    System.out.println("s3");
  }

  @BeforeClass(
      groups = {"g2"},
      dependsOnGroups = "g1",
      priority = 0)
  public void c2() {
    System.out.println("c2");
  }

  @BeforeClass(
      groups = {"g2"},
      dependsOnGroups = "g1",
      priority = 1)
  public void c1() {
    System.out.println("c3");
  }

  @BeforeTest(
      groups = {"g2"},
      dependsOnGroups = "g1",
      priority = 0)
  public void t2() {
    System.out.println("t2");
  }

  @BeforeTest(
      groups = {"g2"},
      dependsOnGroups = "g1",
      priority = 1)
  public void t1() {
    System.out.println("t3");
  }

  @BeforeMethod(
      groups = {"g2"},
      dependsOnGroups = "g1",
      priority = 0)
  public void m2() {
    System.out.println("m2");
  }

  @BeforeMethod(
      groups = {"g2"},
      dependsOnGroups = "g1",
      priority = 1)
  public void m1() {
    System.out.println("m3");
  }

  @Test(
      groups = {"g2"},
      dependsOnGroups = "g1",
      priority = 0)
  public void test2() {
    System.out.println("test2");
  }
}
