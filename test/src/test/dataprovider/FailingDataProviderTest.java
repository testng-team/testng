package test.dataprovider;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;


/**
 * TESTNG-142:
 * Exceptions in DataProvider are not reported as failed test
 */
public class FailingDataProviderTest {
  @Test
  public void failingDataProvider() {
    TestNG testng= new TestNG(false);
    testng.setTestClasses(new Class[] {FailingDataProvider.class});
    TestListenerAdapter tla = new TestListenerAdapter();
    testng.addListener(tla);
    testng.setVerbose(0);
    testng.run();
    Assert.assertEquals(tla.getFailedTests().size(), 1, "Test method should be marked as failed");
  }
}
