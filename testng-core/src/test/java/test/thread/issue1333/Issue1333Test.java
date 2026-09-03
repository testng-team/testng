package test.thread.issue1333;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.assertj.core.api.SoftAssertions;
import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

/**
 * {@code parallel="tests"} promises that all the methods of one {@code <test>} run in the same
 * thread. A suite-level time-out used to break that promise, because it is inherited by every
 * method and a timed method was handed to a fresh executor of its own.
 *
 * <p>Deliberately not a case of {@code test.thread.parallelization.ThreadAffinityTest}: that one
 * turns {@code RuntimeBehavior.TESTNG_THREAD_AFFINITY} on, which measures a different mechanism,
 * and it never runs a configuration method -- whereas the {@code @BeforeClass} landing on a thread
 * of its own is what the reporter of GITHUB-1333 saw first.
 */
public class Issue1333Test extends SimpleBaseTest {

  private static final List<String> TEST_NAMES = Arrays.asList("test1", "test2", "test3");

  /** Comfortably longer than the run: the suite must not actually time out. */
  private static final String SUITE_TIME_OUT_MILLIS = "60000";

  @BeforeMethod
  public void resetRecorder() {
    ThreadIdRecorder.reset();
  }

  /**
   * The regression guard: red before the fix, with one thread per invocation instead of per test.
   */
  @Test(description = "GITHUB-1333")
  public void parallelTestsKeepThreadAffinityWithASuiteTimeOut() {
    XmlSuite suite = parallelTestsSuite();
    suite.setTimeOut(SUITE_TIME_OUT_MILLIS);
    runAndVerifyThreadAffinity(suite);
  }

  /**
   * The control, green before the fix as well. It records the contrast the reporter drew --
   * dropping the suite time-out restored the affinity -- so the time-out is not mistaken for a
   * bystander.
   */
  @Test(description = "GITHUB-1333")
  public void parallelTestsKeepThreadAffinityWithoutASuiteTimeOut() {
    runAndVerifyThreadAffinity(parallelTestsSuite());
  }

  private XmlSuite parallelTestsSuite() {
    XmlSuite suite = createXmlSuite("issue1333");
    suite.setParallel(XmlSuite.ParallelMode.TESTS);
    // Each <test> must be able to claim a worker of its own, or the affinity assertions would hold
    // for a run that never went parallel at all.
    suite.setThreadCount(TEST_NAMES.size());
    TEST_NAMES.forEach(name -> createXmlTest(suite, name, SampleTestClass.class));
    return suite;
  }

  private void runAndVerifyThreadAffinity(XmlSuite suite) {
    TestNG testng = create(suite);
    testng.run();

    assertThat(testng.getStatus()).isZero();

    SoftAssertions softly = new SoftAssertions();
    Set<Long> allThreadIds = new HashSet<>();
    for (String name : TEST_NAMES) {
      Set<Long> threadIds = ThreadIdRecorder.getThreadIds(name);
      softly
          .assertThat(threadIds)
          .as("thread ids used by the invocations of <test name=\"%s\">", name)
          .hasSize(1);
      allThreadIds.addAll(threadIds);
    }
    softly
        .assertThat(allThreadIds)
        .as(
            "distinct thread ids across the <test> tags -- one each, or they did not run in parallel")
        .hasSize(TEST_NAMES.size());
    softly.assertAll();
  }
}
