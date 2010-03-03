package test.dataprovider;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;


/**
 * TESTNG-291:
 * Exceptions thrown by Iterable DataProviders are not caught, no failed test reported
 */
public class FailingIterableDataProviderTest {
  @Test
  public void failingDataProvider() {
    TestNG testng= new TestNG(false);
    testng.setTestClasses(new Class[] {FailingIterableDataProvider.class});
    TestListenerAdapter tla = new TestListenerAdapter();
    testng.addListener(tla);
    testng.setVerbose(0);
    try {
      testng.run();
    } catch (RuntimeException e) {
      Assert.fail("Exceptions thrown during tests should always be caught!", e);
    }
    Assert.assertEquals(tla.getFailedTests().size(), 1,
      "Should have 1 failure from a bad data-provider iteration");
    Assert.assertEquals(tla.getPassedTests().size(), 5,
      "Should have 5 passed test from before the bad data-provider iteration");
    }
}