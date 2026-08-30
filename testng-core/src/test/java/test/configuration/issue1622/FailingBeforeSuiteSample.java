package test.configuration.issue1622;

import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class FailingBeforeSuiteSample {

  public static final List<String> LOGS = new ArrayList<>();

  @BeforeSuite
  public void failingBeforeSuite() {
    LOGS.add("failingBeforeSuite");
    throw new RuntimeException("Simulating a failure in @BeforeSuite");
  }

  @BeforeTest(alwaysRun = true)
  public void beforeTest() {
    LOGS.add("beforeTest");
  }

  @BeforeClass(alwaysRun = true)
  public void beforeClass() {
    LOGS.add("beforeClass");
  }

  @BeforeMethod(alwaysRun = true)
  public void beforeMethod() {
    LOGS.add("beforeMethod");
  }

  @Test
  public void testMethod() {
    LOGS.add("testMethod");
  }

  @AfterMethod(alwaysRun = true)
  public void afterMethod() {
    LOGS.add("afterMethod");
  }

  @AfterClass(alwaysRun = true)
  public void afterClass() {
    LOGS.add("afterClass");
  }

  @AfterTest(alwaysRun = true)
  public void afterTest() {
    LOGS.add("afterTest");
  }

  @AfterSuite(alwaysRun = true)
  public void afterSuite() {
    LOGS.add("afterSuite");
  }
}
