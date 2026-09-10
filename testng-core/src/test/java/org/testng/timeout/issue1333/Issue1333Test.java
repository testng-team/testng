package org.testng.timeout.issue1333;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.assertj.core.api.SoftAssertions;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestNGListener;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.internal.thread.ThreadTimeoutException;
import org.testng.timeout.samples.issue1333.SampleTestClass;
import org.testng.timeout.samples.issue1333.SlowSample;
import org.testng.timeout.samples.issue1333.ThreadIdRecorder;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

/**
 * {@code parallel="tests"} promises that all the methods of one {@code <test>} run in the same
 * thread. A suite-level time-out used to break that promise: the DTD gives such a time-out one
 * meaning per parallel mode -- it aborts "the method (if parallel=methods) or the test (if
 * parallel=tests)" -- but it was inherited as a per-method time-out in every mode, which put each
 * method of a {@code <test>}, configuration methods included, on a timed path and so on a thread of
 * its own.
 *
 * <p>Deliberately not a case of {@code test.thread.parallelization.ThreadAffinityTest}: that one
 * turns {@code RuntimeBehavior.TESTNG_THREAD_AFFINITY} on, which measures a different mechanism,
 * and it never runs a configuration method -- whereas the {@code @BeforeClass} landing on a thread
 * of its own is what the reporter of GITHUB-1333 saw first.
 */
public class Issue1333Test extends SimpleBaseTest {

  private static final List<String> TEST_NAMES = Arrays.asList("test1", "test2", "test3");

  /**
   * Long enough that a healthy run never reaches it -- the run takes tens of milliseconds -- and
   * short enough that a wedged {@code <test>} worker is capped rather than stalling the build. The
   * whole suite runs once per Gradle fork, so a stall is paid once per fork.
   */
  private static final long SUITE_TIME_OUT_MILLIS = 10_000L;

  /** A third of {@code SlowSample}'s sleep: enforced returns at 1s, dropped at 3s. */
  private static final long TEST_TIME_OUT_MILLIS = 1_000L;

  @BeforeMethod
  public void resetRecorder() {
    ThreadIdRecorder.reset();
  }

  /**
   * The behavioural guard: red before the fix, with one thread per invocation instead of one per
   * {@code <test>}.
   */
  @Test(description = "GITHUB-1333")
  public void parallelTestsKeepThreadAffinityWithASuiteTimeOut() {
    XmlSuite suite = parallelTestsSuite();
    suite.setTimeOut(Long.toString(SUITE_TIME_OUT_MILLIS));

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

  /**
   * The contract behind that guard, asserted directly: under {@code parallel="tests"} a suite-level
   * time-out bounds the {@code <test>}, so no method inherits it, while every other mode still
   * inherits it as before. Reading the two modes in one test is what makes this discriminate -- a
   * build that inherits the time-out everywhere fails the first assertion, one that inherits it
   * nowhere fails the second.
   */
  @Test(description = "GITHUB-1333")
  public void aSuiteTimeOutBoundsTheTestRatherThanEachMethodUnderParallelTests() {
    Map<String, Long> underTests = timeOutsSeenBy(XmlSuite.ParallelMode.TESTS);
    Map<String, Long> underClasses = timeOutsSeenBy(XmlSuite.ParallelMode.CLASSES);

    SoftAssertions softly = new SoftAssertions();
    softly
        .assertThat(underTests)
        .as("per-method time-outs under parallel=tests")
        .isNotEmpty()
        .allSatisfy((method, timeOut) -> assertThat(timeOut).isZero());
    softly
        .assertThat(underClasses)
        .as("per-method time-outs under parallel=classes, which the DTD still bounds per method")
        .isNotEmpty()
        .allSatisfy((method, timeOut) -> assertThat(timeOut).isEqualTo(SUITE_TIME_OUT_MILLIS));
    softly.assertAll();
  }

  /**
   * A time-out declared on the {@code <test>} itself is not the suite's: under {@code
   * parallel="tests"} nothing enforces it at {@code <test>} scope -- {@code SuiteRunner} bounds its
   * workers with the <em>suite</em> value only, and {@code TestTaskExecutor}, which would bound a
   * {@code <test>}, is not used since {@code ParallelMode.TESTS} is not parallel at test level. So
   * it must keep reaching the method, or it is silently dropped.
   */
  @Test(description = "GITHUB-1333")
  public void aTestTimeOutIsStillEnforcedUnderParallelTests() {
    XmlSuite suite = createXmlSuite("issue1333");
    suite.setParallel(XmlSuite.ParallelMode.TESTS);
    createXmlTest(suite, "slow", SlowSample.class).setTimeOut(TEST_TIME_OUT_MILLIS);

    assertSlowIsCutShort(suite);
  }

  /**
   * The mirror case: {@code parallel="tests"} on the {@code <test>} while the suite is not in that
   * mode. {@code XmlTest.getParallel()} answers {@code TESTS}, but the parallel-tests path -- and
   * with it the suite-level bound -- is selected from {@code XmlSuite.getParallel()}, so here too
   * only the method-level inheritance enforces anything.
   */
  @Test(description = "GITHUB-1333")
  public void aTestTimeOutIsStillEnforcedWhenOnlyTheTestSaysParallelTests() {
    XmlSuite suite = createXmlSuite("issue1333");
    XmlTest test = createXmlTest(suite, "slow", SlowSample.class);
    test.setParallel(XmlSuite.ParallelMode.TESTS);
    test.setTimeOut(TEST_TIME_OUT_MILLIS);

    assertSlowIsCutShort(suite);
  }

  private void assertSlowIsCutShort(XmlSuite suite) {
    TestNG testng = create(suite);
    TestListenerAdapter tla = new TestListenerAdapter();
    testng.addListener((ITestNGListener) tla);
    testng.run();

    assertThat(tla.getPassedTests())
        .as("slow() must not pass by outrunning its time-out")
        .isEmpty();
    assertThat(tla.getFailedTests()).hasSize(1);
    ITestResult failed = tla.getFailedTests().get(0);
    assertThat(failed.getThrowable()).isInstanceOf(ThreadTimeoutException.class);
    assertThat(failed.getEndMillis() - failed.getStartMillis())
        .as("slow() must be cut short at its time-out, not sleep its full 3s")
        .isLessThan(2 * TEST_TIME_OUT_MILLIS);
  }

  private Map<String, Long> timeOutsSeenBy(XmlSuite.ParallelMode mode) {
    XmlSuite suite = parallelTestsSuite();
    suite.setParallel(mode);
    suite.setTimeOut(Long.toString(SUITE_TIME_OUT_MILLIS));

    Map<String, Long> timeOuts = new ConcurrentHashMap<>();
    TestNG testng = create(suite);
    testng.addListener(
        (ITestNGListener)
            new IInvokedMethodListener() {
              @Override
              public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
                timeOuts.put(
                    method.getTestMethod().getMethodName(), method.getTestMethod().getTimeOut());
              }
            });
    testng.run();

    assertThat(testng.getStatus()).isZero();
    return timeOuts;
  }

  private XmlSuite parallelTestsSuite() {
    XmlSuite suite = createXmlSuite("issue1333");
    suite.setParallel(XmlSuite.ParallelMode.TESTS);
    // Pins the pool to exactly one worker per <test>. The default is already 5 (XmlSuite
    // DEFAULT_THREAD_COUNT), so this lowers it rather than raising it; it is here to make the
    // "one thread per <test>" assertion below say what it means.
    suite.setThreadCount(TEST_NAMES.size());
    TEST_NAMES.forEach(name -> createXmlTest(suite, name, SampleTestClass.class));
    return suite;
  }
}
