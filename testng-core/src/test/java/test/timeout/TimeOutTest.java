package test.timeout;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.internal.thread.ThreadTimeoutException;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.ParallelMode;
import test.BaseTest;
import test.timeout.github1493.TestClassSample;
import test.timeout.issue2009.TimeOutWithParallelSample;

public class TimeOutTest extends BaseTest {
  private final long m_id;

  public TimeOutTest() {
    m_id = System.currentTimeMillis();
  }

  private void privateTimeOutTest(XmlSuite.ParallelMode parallel) {
    addClass(TimeOutSampleTest.class);
    if (parallel != null) {
      setParallel(parallel);
    }
    run();

    verifyPassedTests("timeoutShouldPass");
    verifyFailedTests("timeoutShouldFailByException", "timeoutShouldFailByTimeOut");
  }

  @DataProvider(name = "parallelModes")
  public Iterator<Object[]> createData() {
    final Iterator<XmlSuite.ParallelMode> parallelModes =
        Arrays.asList(XmlSuite.ParallelMode.values()).iterator();
    return new Iterator<Object[]>() {
      @Override
      public boolean hasNext() {
        return parallelModes.hasNext();
      }

      @Override
      public Object[] next() {
        return new Object[] {parallelModes.next()};
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("remove");
      }
    };
  }

  @Test(dataProvider = "parallelModes")
  public void timeOutInParallel(XmlSuite.ParallelMode parallelMode) {
    privateTimeOutTest(parallelMode);
  }

  @Test
  public void timeOutInNonParallel() {
    privateTimeOutTest(null);
  }

  @Test
  public void verifyInvocationTimeOut() {
    addClass(InvocationTimeOutSampleTest.class);
    run();
    verifyPassedTests("shouldPass");
    verifyFailedTests("shouldFail");
  }

  @Test
  public void testWithOnlyOneThread() {
    addClass(TestClassSample.class);
    run();
    Collection<List<ITestResult>> failed = getFailedTests().values();
    Assert.assertEquals(failed.size(), 1);
    ITestResult failedResult = failed.iterator().next().get(0);
    Assert.assertTrue(failedResult.getThrowable() instanceof ThreadTimeoutException);
    Assert.assertEquals(
        failedResult.getThrowable().getMessage(),
        String.format(
            "Method %s.testMethod() didn't finish within the time-out 1000",
            TestClassSample.class.getName()));
  }

  @Test(description = "GITHUB-2009")
  public void testTimeOutWhenParallelIsMethods() {
    addClass(TimeOutWithParallelSample.class);
    setParallel(ParallelMode.METHODS);
    run();
    Assert.assertEquals(getFailedTests().values().size(), 1);
    Assert.assertEquals(getSkippedTests().values().size(), 0);
    Assert.assertEquals(getPassedTests().values().size(), 0);
    ITestResult result = getFailedTests().values().iterator().next().get(0);
    long time = result.getEndMillis() - result.getStartMillis();
    Assert.assertTrue(time < 2000);
  }

  @Override
  public Long getId() {
    return m_id;
  }
}
