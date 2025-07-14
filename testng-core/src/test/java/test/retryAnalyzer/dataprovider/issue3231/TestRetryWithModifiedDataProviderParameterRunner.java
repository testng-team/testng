package test.retryAnalyzer.dataprovider.issue3231;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

import static org.testng.Assert.assertEquals;

public class TestRetryWithModifiedDataProviderParameterRunner extends SimpleBaseTest {

  @Test(timeOut = 5000)
  public void willNotStopAfter3Failures() {
    TestNG tng = create(RetryWithModifiedDataProviderParameterTest.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();

    assertEquals(tla.getPassedTests().size(), 0);
    assertEquals(tla.getFailedTests().size(), 1);
    assertEquals(tla.getSkippedTests().size(), 2);
  }
}
