package test.invocationcount;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class FailedInvocationCountTest extends SimpleBaseTest {

  private void runTest(boolean skip, int passed, int failed, int skipped) {
    TestNG testng = create(FailedInvocationCount.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    testng.setSkipFailedInvocationCounts(skip);
    testng.addListener(tla);
    testng.run();

    Assert.assertEquals(tla.getPassedTests().size(), passed);
    Assert.assertEquals(tla.getFailedTests().size(), failed);
    Assert.assertEquals(tla.getSkippedTests().size(), skipped);
  }

  @Test
  public void verifyGloballyShouldStop() {
    runTest(true, 4, 1, 5);
  }

  @Test
  public void verifyGloballyShouldNotStop() {
    runTest(false, 4, 6, 0);
  }

  @Test
  public void verifyAttributeShouldStop() {
    TestNG testng = create(FailedInvocationCount2.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    testng.addListener(tla);
    testng.run();

    Assert.assertEquals(tla.getPassedTests().size(), 8);
    Assert.assertEquals(tla.getFailedTests().size(), 7);
    Assert.assertEquals(tla.getSkippedTests().size(), 5);
  }
}
