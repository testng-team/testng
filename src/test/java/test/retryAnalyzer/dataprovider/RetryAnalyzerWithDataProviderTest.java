package test.retryAnalyzer.dataprovider;

import static org.testng.Assert.assertEquals;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

public class RetryAnalyzerWithDataProviderTest extends SimpleBaseTest {

  @Test
  public void testRetryCounts() {
    TestNG tng = create(RetryCountTest.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);

    tng.run();

    // TODO re-enabled after #740 or #1104 is fixed/merged
//    assertEquals(tla.getPassedTests().size(), 1);
    assertEquals(tla.getPassedTests().get(0).getParameters(), new String[]{"c"});

    assertEquals(tla.getFailedTests().size(), 3);
    assertEquals(tla.getSkippedTests().size(), 9);
  }

}
