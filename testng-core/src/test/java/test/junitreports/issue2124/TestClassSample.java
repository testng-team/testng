package test.junitreports.issue2124;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestClassSample {

  public static final String MESSAGE_1 = "Teenage Mutant Ninja Turtles";
  public static final String MESSAGE_2 = "Teenage Mutant Ninja Turtles: <i>Out of the Shadows</i>";
  public static final String MESSAGE_3 =
      "Teenage Mutant Ninja Turtles: <i>The Secret of the Ooze</i>";
  public static final String MESSAGE_4 = "Teenage Mutant Ninja Turtles: <i>Mutant Mayhem</i>";
  public static final String MESSAGE_5 =
      "Teenage Mutant Ninja Turtles: <i>Rise of the Teenage Mutant Ninja Turtles</i>";
  public static final String MESSAGE_BEFORE = "Teenage Mutant Ninja Turtles Movies";
  public static final String MESSAGE_AFTER = "To be continued";
  public static final String MESSAGE_FAIL = "Cowabunga";

  public static final String TEST_METHOD_WITH_REPORTER = "testReporter";
  public static final String TEST_METHOD_WITH_DATA_PROVIDER_REPORTER =
      "testReporterWithDataProvider";
  public static final String TEST_METHOD_FAIL_WITH_REPORTER = "testFailWithReporter";
  public static final String TEST_METHOD_FAIL_NO_REPORTER = "testFailNoReporter";
  public static final String TEST_METHOD_NO_REPORTER = "testNoReporter";

  @BeforeSuite
  public void before() {
    Reporter.log(MESSAGE_BEFORE, true);
  }

  @AfterSuite
  public void after() {
    Reporter.log(MESSAGE_AFTER, true);
  }

  @Test
  public void testReporter() {
    Reporter.log(MESSAGE_1, true);
    Reporter.log(MESSAGE_2, true);
  }

  @DataProvider(name = "testReporterDataProvider")
  public Object[][] testReporterDataProvider() {
    return new Object[][] {{MESSAGE_3}, {MESSAGE_4}};
  }

  @Test(dataProvider = "testReporterDataProvider")
  public void testReporterWithDataProvider(String message) {
    Reporter.log(message, true);
  }

  @Test
  public void testFailWithReporter() {
    Reporter.log(MESSAGE_5, true);
    Assert.fail(MESSAGE_FAIL);
  }

  @Test
  public void testFailNoReporter() {
    Assert.fail(MESSAGE_FAIL);
  }

  @Test
  public void testNoReporter() {}
}
