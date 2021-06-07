package test.configuration.issue2400;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class TestNGTestClass implements TestRunnerCapabilities {

  @BeforeSuite
  @Override
  public void beforeSuite() {
    DataStore.INSTANCE.increment("beforeSuite");
  }

  @BeforeTest
  @Override
  public void beforeTest() {
    DataStore.INSTANCE.increment("beforeTest");
  }

  @BeforeClass
  @Override
  public void beforeClass() {
    DataStore.INSTANCE.increment("beforeClass");
  }

  @BeforeMethod
  @Override
  public void beforeMethod() {
    DataStore.INSTANCE.increment("beforeMethod");
  }

  @Test
  public void testMethod() {}

  @AfterMethod
  @Override
  public void afterMethod() {
    DataStore.INSTANCE.increment("afterMethod");
  }

  @AfterClass
  @Override
  public void afterClass() {
    DataStore.INSTANCE.increment("afterClass");
  }

  @AfterTest
  @Override
  public void afterTest() {
    DataStore.INSTANCE.increment("afterTest");
  }

  @AfterSuite
  @Override
  public void afterSuite() {
    DataStore.INSTANCE.increment("afterSuite");
  }
}
