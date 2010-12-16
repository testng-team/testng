package test.dataprovider;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.Test;


public class FailingDataProviderTest {
  @Test(description = "TESTNG-142: Exceptions in DataProvider are not reported as failed test")
  public void failingDataProvider() {
    TestNG testng= new TestNG(false);
    testng.setTestClasses(new Class[] {FailingDataProvider.class});
    TestListenerAdapter tla = new TestListenerAdapter();
    testng.addListener(tla);
    testng.setVerbose(0);
    testng.run();
    Assert.assertEquals(tla.getSkippedTests().size(), 1, "Test method should be marked as skipped");
  }

  @Test(expectedExceptions = TestNGException.class,
      description = "TESTNG-447: abort when two data providers have the same name")
  public void duplicateDataProviders() {
    TestNG testng= new TestNG(false);
    testng.setTestClasses(new Class[] {DuplicateDataProviderSampleTest.class});
    testng.run();
  }
}
