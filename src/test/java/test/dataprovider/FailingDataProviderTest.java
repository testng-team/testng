package test.dataprovider;

import org.testng.Assert;
import org.testng.ITestNGListener;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

public class FailingDataProviderTest extends SimpleBaseTest {

  private static void shouldSkip(Class<?> cls, String message, int expected) {
    TestListenerAdapter tla = run(cls);
    Assert.assertEquals(tla.getSkippedTests().size(), expected, message);
  }

  private static void shouldFail(Class<?> cls, String message, int expected) {
    TestListenerAdapter tla = run(cls);
    Assert.assertEquals(tla.getFailedTests().size(), expected, message);
  }

  private static TestListenerAdapter run(Class<?> cls) {
    TestNG testng = create(cls);
    TestListenerAdapter tla = new TestListenerAdapter();
    testng.addListener((ITestNGListener) tla);
    testng.run();
    return tla;
  }

  @Test(description = "TESTNG-142: Exceptions in DataProvider are not reported as failed test")
  public void failingDataProvider() {
    shouldSkip(FailingDataProvider.class, "Test method should be marked as skipped", 1);
  }

  @Test(description = "TESTNG-447: Abort when two data providers have the same name")
  public void duplicateDataProviders() {
    shouldFail(DuplicateDataProviderSampleTest.class, "", 1);
  }

  @Test
  public void failingDataProviderAndInvocationCount() throws Exception {
    shouldSkip(DataProviderWithError.class,
        "Test should be skipped even if invocation counter and success percentage set", 4);
  }
}
