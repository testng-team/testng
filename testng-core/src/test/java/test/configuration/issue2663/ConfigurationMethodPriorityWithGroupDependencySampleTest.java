package test.configuration.issue2663;

import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class ConfigurationMethodPriorityWithGroupDependencySampleTest {
  public static List<String> logs = new ArrayList<>();

  @BeforeSuite(
      groups = {"g1"},
      priority = 2)
  public void beforeSuite3() {
    print();
  }

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
      groups = {"g1"},
      priority = 2)
  public void beforeClass3() {
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
      groups = {"g1"},
      priority = 2)
  public void beforeTest3() {
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
      groups = {"g1"},
      priority = 2)
  public void beforeMethod3() {
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

  @Test(groups = {"g1"})
  public void test3() {
    print();
  }

  @Test(
      groups = {"g2"},
      dependsOnGroups = "g1",
      priority = 0)
  public void test2() {
    print();
  }

  @Test(
      groups = {"g3"},
      dependsOnGroups = "g1",
      priority = 1)
  public void test1() {
    print();
  }

  private synchronized void print() {
    StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
    String methodName =
        stackTraceElements[2].getClassName() + "." + stackTraceElements[2].getMethodName();
    logs.add(methodName);
    System.out.println(methodName);
  }
}
