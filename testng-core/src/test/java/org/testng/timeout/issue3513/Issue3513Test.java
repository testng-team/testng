package org.testng.timeout.issue3513;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.ITestNGListener;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.internal.thread.ThreadTimeoutException;
import org.testng.timeout.samples.issue3513.FastSample;
import org.testng.timeout.samples.issue3513.InterruptibleSample;
import org.testng.timeout.samples.issue3513.StubbornSample;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

/**
 * Under {@code parallel="tests"}, {@code SuiteRunner} bounds each {@code <test>} with the suite
 * time-out through {@code invokeAll}. When that bound fires, a method that ignores interruption
 * keeps its worker running, {@code runTest} never records a result, and the run exits 0. The same
 * suite with {@code parallel="none"} reports the method as failed and exits 1.
 */
public class Issue3513Test extends SimpleBaseTest {

  /**
   * Long enough that an empty {@code <test>} always finishes, and short enough that a 2s sample is
   * cut rather than run to completion. The whole suite runs once per Gradle fork.
   */
  private static final long SUITE_TIME_OUT_MILLIS = 400L;

  @Test(description = "GITHUB-3513")
  public void aMethodThatIgnoresInterruptionIsReportedWhenParallelTestsTimesOut() {
    XmlSuite suite = createXmlSuite("issue3513");
    suite.setParallel(XmlSuite.ParallelMode.TESTS);
    suite.setThreadCount(2);
    suite.setTimeOut(Long.toString(SUITE_TIME_OUT_MILLIS));
    createXmlTest(suite, "stubborn-test", StubbornSample.class);
    createXmlTest(suite, "fast-test", FastSample.class);

    TestNG testng = create(suite);
    TestListenerAdapter tla = new TestListenerAdapter();
    testng.addListener((ITestNGListener) tla);
    testng.run();

    assertThat(testng.getStatus()).isNotZero();
    assertThat(tla.getPassedTests()).extracting(ITestResult::getName).containsExactly("fast");
    assertThat(tla.getFailedTests()).hasSize(1);
    ITestResult failed = tla.getFailedTests().get(0);
    assertThat(failed.getName()).isEqualTo("stubborn");
    assertThat(failed.getThrowable()).isInstanceOf(ThreadTimeoutException.class);
  }

  @Test(description = "GITHUB-3513")
  public void anInterruptibleOverrunIsCountedWhenParallelTestsTimesOut() {
    XmlSuite suite = createXmlSuite("issue3513");
    suite.setParallel(XmlSuite.ParallelMode.TESTS);
    suite.setThreadCount(2);
    suite.setTimeOut(Long.toString(SUITE_TIME_OUT_MILLIS));
    createXmlTest(suite, "slow-test", InterruptibleSample.class);
    createXmlTest(suite, "fast-test", FastSample.class);

    TestNG testng = create(suite);
    TestListenerAdapter tla = new TestListenerAdapter();
    testng.addListener((ITestNGListener) tla);
    testng.run();

    assertThat(testng.getStatus()).isNotZero();
    assertThat(tla.getPassedTests()).extracting(ITestResult::getName).containsExactly("fast");
    assertThat(tla.getFailedTests()).extracting(ITestResult::getName).contains("interruptible");
  }
}
