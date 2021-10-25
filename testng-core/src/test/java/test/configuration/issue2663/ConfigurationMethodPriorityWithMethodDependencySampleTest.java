package test.configuration.issue2663;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class ConfigurationMethodPriorityWithMethodDependencySampleTest {

  @BeforeSuite(priority = 2)
  public void s3() {
    System.out.println("s3");
  }

  @BeforeSuite(dependsOnMethods = "s3", priority = 0)
  public void s2() {
    System.out.println("s2");
  }

  @BeforeSuite(dependsOnMethods = "s3", priority = 1)
  public void s1() {
    System.out.println("s1");
  }

  @BeforeClass(priority = 2)
  public void c3() {
    System.out.println("c3");
  }

  @BeforeClass(dependsOnMethods = "c3", priority = 0)
  public void c2() {
    System.out.println("c2");
  }

  @BeforeClass(dependsOnMethods = "c3", priority = 1)
  public void c1() {
    System.out.println("c1");
  }

  @BeforeTest(priority = 2)
  public void t3() {
    System.out.println("t3");
  }

  @BeforeTest(dependsOnMethods = "t3", priority = 0)
  public void t2() {
    System.out.println("t2");
  }

  @BeforeTest(dependsOnMethods = "t3", priority = 1)
  public void t1() {
    System.out.println("t1");
  }

  @BeforeMethod(priority = 2)
  public void m3() {
    System.out.println("m3");
  }

  @BeforeMethod(dependsOnMethods = "m3", priority = 0)
  public void m2() {
    System.out.println("m2");
  }

  @BeforeMethod(dependsOnMethods = "m3", priority = 1)
  public void m1() {
    System.out.println("m1");
  }

  @Test(groups = {"g1"})
  public void test3() {
    System.out.println("test3");
  }

  @Test(dependsOnMethods = "test3", priority = 0)
  public void test2() {
    System.out.println("test2");
  }

  @Test(dependsOnMethods = "test3", priority = 1)
  public void test1() {
    System.out.println("test1");
  }
}
