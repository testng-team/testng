package test.configuration.issue2663;

import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class ConfigurationMethodPriorityWithMethodDependencySampleTest {
  public static List<String> logs = new ArrayList<>();

  @BeforeSuite(priority = 2)
  public void beforeSuite3() {
    print();
  }

  @BeforeSuite(dependsOnMethods = "beforeSuite3", priority = 0)
  public void beforeSuite2() {
    print();
  }

  @BeforeSuite(dependsOnMethods = "beforeSuite3", priority = 1)
  public void beforeSuite1() {
    print();
  }

  @BeforeClass(priority = 2)
  public void beforeClass3() {
    print();
  }

  @BeforeClass(dependsOnMethods = "beforeClass3", priority = 0)
  public void beforeClass2() {
    print();
  }

  @BeforeClass(dependsOnMethods = "beforeClass3", priority = 1)
  public void beforeClass1() {
    print();
  }

  @BeforeTest(priority = 2)
  public void beforeTest3() {
    print();
  }

  @BeforeTest(dependsOnMethods = "beforeTest3", priority = 0)
  public void beforeTest2() {
    print();
  }

  @BeforeTest(dependsOnMethods = "beforeTest3", priority = 1)
  public void beforeTest1() {
    print();
  }

  @BeforeMethod(priority = 2)
  public void beforeMethod3() {
    print();
  }

  @BeforeMethod(dependsOnMethods = "beforeMethod3", priority = 0)
  public void beforeMethod2() {
    print();
  }

  @BeforeMethod(dependsOnMethods = "beforeMethod3", priority = 1)
  public void beforeMethod1() {
    print();
  }

  @Test(groups = {"g1"})
  public void test3() {
    print();
  }

  @Test(dependsOnMethods = "test3", priority = 0)
  public void test2() {
    print();
  }

  @Test(dependsOnMethods = "test3", priority = 1)
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
