package test.retryAnalyzer.dataprovider;

import static org.testng.Assert.assertEquals;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class RetryAnalyzerWithComplexDataProviderTest extends SimpleBaseTest {

  // Test without data-provider with end result as success after 3 retry attempts
  @Test(description = "GITHUB-2148")
  public void testWithoutDataProvider() {
    TestNG testng = create(RetryAnalyzerWithoutDataProvider.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    testng.addListener(tla);
    testng.run();
    assertEquals(tla.getPassedTests().size(), 1);
    assertEquals(tla.getFailedTests().size(), 0);
    assertEquals(tla.getSkippedTests().size(), 3);
  }

  // Test with string array of objects in data-provider with end result as success after 3 retry
  // attempts
  @Test(description = "GITHUB-2148")
  public void testWithDataProviderStringArray() {
    TestNG testng = create(DataProviderWithStringArraySample.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    testng.addListener(tla);
    testng.run();
    assertEquals(tla.getPassedTests().size(), 1);
    assertEquals(tla.getFailedTests().size(), 0);
    assertEquals(tla.getSkippedTests().size(), 3);
  }

  // Test with multiple integer arrays in data-provider when parallel mode on and end result as
  // success
  // after 3 retry attempts
  @Test(description = "GITHUB-2148")
  public void testWithSingleParam() {
    TestNG testng = create(DataProviderWithSingleParam.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    testng.addListener(tla);
    testng.run();
    // Since 2 integer arrays are passing inside data-provider, number of test cases should be 2
    assertEquals(tla.getPassedTests().size(), 2);
    assertEquals(tla.getFailedTests().size(), 0);
    assertEquals(
        tla.getSkippedTests().size(),
        6); // Number of total skipped count should be 6, each for a test
  }

  // Test with objects in data-provider with result as success after 3 retry attempts
  @Test(description = "GITHUB-2148")
  public void testWithDataProviderWithObjectAndArraySample() {
    TestNG testng = create(ComplexDataProviderWithObjectAndArraySample.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    testng.addListener(tla);
    testng.run();
    assertEquals(tla.getPassedTests().size(), 1);
    assertEquals(tla.getFailedTests().size(), 0);
    assertEquals(tla.getSkippedTests().size(), 3);
  }

  // Test with objects in data-provider with result as failed after 3 retry attempts
  @Test(description = "GITHUB-2148")
  public void testDataProviderWithRetryAttemptsFailure() {
    TestNG testng = create(DataProviderWithRetryAttemptsFailure.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    testng.addListener(tla);
    testng.run();
    assertEquals(tla.getFailedTests().size(), 1);
    assertEquals(tla.getPassedTests().size(), 0);
    assertEquals(tla.getSkippedTests().size(), 3);
  }
}
