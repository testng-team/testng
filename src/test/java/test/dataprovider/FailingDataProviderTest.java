package test.dataprovider;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

public class FailingDataProviderTest extends SimpleBaseTest {
  private void shouldSkipOne(Class cls, String message) {
    TestNG testng = create(cls);
    TestListenerAdapter tla = new TestListenerAdapter();
    testng.addListener(tla);
    testng.run();
    Assert.assertEquals(tla.getSkippedTests().size(), 1, message);
  }

  @Test(description = "TESTNG-142: Exceptions in DataProvider are not reported as failed test")
  public void failingDataProvider() {
    shouldSkipOne(FailingDataProvider.class, "Test method should be marked as skipped");
  }

  @Test(description = "TESTNG-447: Abort when two data providers have the same name")
  public void duplicateDataProviders() {
    shouldSkipOne(DuplicateDataProviderSampleTest.class, "");
  }
}
