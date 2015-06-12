package test.dataprovider;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

public class FailingDataProviderTest extends SimpleBaseTest {
  private void shouldSkip(Class cls, String message, int expected) {
    TestNG testng = create(cls);
    TestListenerAdapter tla = new TestListenerAdapter();
    testng.addListener(tla);
    testng.run();
    Assert.assertEquals(tla.getSkippedTests().size(), expected, message);
  }

  @Test(description = "TESTNG-142: Exceptions in DataProvider are not reported as failed test")
  public void failingDataProvider() {
    shouldSkip(FailingDataProvider.class, "Test method should be marked as skipped", 1);
  }

  @Test(description = "TESTNG-447: Abort when two data providers have the same name")
  public void duplicateDataProviders() {
    shouldSkip(DuplicateDataProviderSampleTest.class, "", 1);
  }

  @Test
  public void failingDataProviderAndInvocationCount() throws Exception {
    shouldSkip(DataProviderWithError.class,
        "Test should be skipped even if invocation counter and success percentage set", 4);
  }
}
