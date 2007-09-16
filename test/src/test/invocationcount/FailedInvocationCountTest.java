package test.invocationcount;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

public class FailedInvocationCountTest {
  
  private void runTest(boolean skip,
    int passed, int failed, int skipped)
  {
    TestNG testng = new TestNG();
    testng.setVerbose(0);
    testng.setSkipFailedInvocationCounts(skip);
    testng.setTestClasses(new Class[] { FailedInvocationCount.class });
    TestListenerAdapter tla = new TestListenerAdapter();
    testng.addListener(tla);
    testng.run();
    
    Assert.assertEquals(tla.getPassedTests().size(), passed);
    Assert.assertEquals(tla.getFailedTests().size(), failed);
    Assert.assertEquals(tla.getSkippedTests().size(), skipped);
  }
  
  @Test
  public void failedShouldStop() {
    runTest(true, 4, 1, 5);
  }
  
  @Test
  public void failedShouldNotStop() {
    runTest(false, 4, 6, 0);
  }
}
