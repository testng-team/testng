package test.configuration.issue2663;

import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class ConfigurationMethodPrioritySampleTest {
  public static List<String> logs = new ArrayList<>();

  @BeforeSuite(priority = 100)
  public void beforeSuiteA() {
    print();
  }

  @BeforeSuite(priority = 1)
  public void beforeSuiteB() {
    print();
  }

  @BeforeClass(priority = 100)
  public void beforeClassA() {
    print();
  }

  @BeforeClass(priority = 1)
  public void beforeClassB() {
    print();
  }

  @BeforeMethod(priority = 100)
  public void beforeMethodA() {
    print();
  }

  @BeforeMethod(priority = 1)
  public void beforeMethodB() {
    print();
  }

  @Test(priority = 100)
  public void testA() {
    print();
  }

  @Test(priority = 0)
  public void testB() {
    print();
  }

  @AfterSuite(priority = 100)
  public void afterSuiteA() {
    print();
  }

  @AfterSuite(priority = 1)
  public void afterSuiteB() {
    print();
  }

  @AfterClass(priority = 100)
  public void afterClassA() {
    print();
  }

  @AfterClass(priority = 1)
  public void afterClassB() {
    print();
  }

  @AfterMethod(priority = 100)
  public void afterMethodA() {
    print();
  }

  @AfterMethod(priority = 1)
  public void afterMethodB() {
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
