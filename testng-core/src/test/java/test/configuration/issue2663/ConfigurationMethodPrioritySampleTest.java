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
    logs.add("beforeSuiteA");
  }

  @BeforeSuite(priority = 1)
  public void beforeSuiteB() {
    logs.add("beforeSuiteB");
  }

  @BeforeClass(priority = 100)
  public void beforeClassA() {
    logs.add("beforeClassA");
  }

  @BeforeClass(priority = 1)
  public void beforeClassB() {
    logs.add("beforeClassB");
  }

  @BeforeMethod(priority = 100)
  public void beforeMethodA() {
    logs.add("beforeMethodA");
  }

  @BeforeMethod(priority = 1)
  public void beforeMethodB() {
    logs.add("beforeMethodB");
  }

  @Test(priority = 100)
  public void testA() {
    logs.add("TestA");
  }

  @Test(priority = 0)
  public void testB() {
    logs.add("TestB");
  }

  @AfterSuite(priority = 100)
  public void afterSuiteA() {
    logs.add("afterSuiteA");
  }

  @AfterSuite(priority = 1)
  public void afterSuiteB() {
    logs.add("afterSuiteB");
  }

  @AfterClass(priority = 100)
  public void afterClassA() {
    logs.add("afterClassA");
  }

  @AfterClass(priority = 1)
  public void afterClassB() {
    logs.add("afterClassB");
  }

  @AfterMethod(priority = 100)
  public void afterMethodA() {
    logs.add("afterMethodA");
  }

  @AfterMethod(priority = 1)
  public void afterMethodB() {
    logs.add("afterMethodB");
  }
}
