package test.configuration.issue2400;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;

public interface TestRunnerCapabilities {

  @BeforeSuite
  default void beforeSuite() {
    DataStore.INSTANCE.increment("beforeSuite");
  }

  @BeforeTest
  default void beforeTest() {
    DataStore.INSTANCE.increment("beforeTest");
  }

  @BeforeClass
  default void beforeClass() {
    DataStore.INSTANCE.increment("beforeClass");
  }

  @BeforeMethod
  default void beforeMethod() {
    DataStore.INSTANCE.increment("beforeMethod");
  }

  @AfterMethod
  default void afterMethod() {
    DataStore.INSTANCE.increment("afterMethod");
  }

  @AfterClass
  default void afterClass() {
    DataStore.INSTANCE.increment("afterClass");
  }

  @AfterTest
  default void afterTest() {
    DataStore.INSTANCE.increment("afterTest");
  }

  @AfterSuite
  default void afterSuite() {
    DataStore.INSTANCE.increment("afterSuite");
  }
}
