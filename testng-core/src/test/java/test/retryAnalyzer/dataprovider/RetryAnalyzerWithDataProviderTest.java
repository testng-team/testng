package test.retryAnalyzer.dataprovider;

import static org.testng.Assert.assertEquals;

import org.testng.ITestNGListener;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

public class RetryAnalyzerWithDataProviderTest extends SimpleBaseTest {

  @Test
  public void testRetryCounts() {
    TestNG tng = create(RetryCountTest.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener((ITestNGListener) tla);

    tng.run();

    assertEquals(tla.getPassedTests().size(), 1);
    assertEquals(tla.getPassedTests().get(0).getParameters(), new String[]{"c"});

    assertEquals(tla.getFailedTests().size(), 3);
    assertEquals(tla.getSkippedTests().size(), 9);
  }

}
