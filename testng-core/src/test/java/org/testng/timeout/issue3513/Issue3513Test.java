package org.testng.timeout.issue3513;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGListener;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.internal.ExitCode;
import org.testng.internal.IConfiguration;
import org.testng.internal.thread.ThreadTimeoutException;
import org.testng.internal.thread.ThreadUtil;
import org.testng.reporters.FailedReporter;
import org.testng.reporters.RuntimeBehavior;
import org.testng.timeout.samples.issue3513.AfterMethodBlockedSample;
import org.testng.timeout.samples.issue3513.FastSample;
import org.testng.timeout.samples.issue3513.InterceptorSample;
import org.testng.timeout.samples.issue3513.InterruptibleSample;
import org.testng.timeout.samples.issue3513.MixedDataProviderSample;
import org.testng.timeout.samples.issue3513.MixedInvocationSample;
import org.testng.timeout.samples.issue3513.StubbornSample;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

/**
 * Under {@code parallel="tests"}, {@code SuiteRunner} bounds each {@code <test>} with the suite
 * time-out through {@code invokeAll}. When that bound fires, a method that ignores interruption
 * keeps its worker running. {@code runTest} never records a result, and the run exits 0. The same
 * suite with {@code parallel="none"} reports the method as failed and exits 1.
 */
public class Issue3513Test extends SimpleBaseTest {

  /**
   * Long enough that an empty {@code <test>} always finishes, and short enough that a 2s sample is
   * cut rather than run to completion. The whole suite runs once per Gradle fork.
   */
  private static final long SUITE_TIME_OUT_MILLIS = 400L;

  @Test(description = "GITHUB-3513")
  public void aMethodThatIgnoresInterruptionIsReportedWhenParallelTestsTimesOut()
      throws IOException, InterruptedException {
    File outputDir = createDirInTempDir("issue3513-stubborn");
    Run run =
        runParallelSuite(outputDir, "stubborn-test", StubbornSample.class, /* interceptor */ null);

    assertThat(run.testng.getStatus()).isEqualTo(ExitCode.FAILED);
    assertThat(run.tla.getPassedTests()).extracting(ITestResult::getName).containsExactly("fast");
    assertThat(run.tla.getFailedTests()).hasSize(1);
    ITestResult failed = run.tla.getFailedTests().get(0);
    assertThat(failed.getName()).isEqualTo("stubborn");
    assertThat(failed.getThrowable()).isInstanceOf(ThreadTimeoutException.class);
    assertReportsContainFailedMethod(outputDir, "stubborn", true);

    Thread.sleep(2_500);
    assertThat(run.tla.getPassedTests()).extracting(ITestResult::getName).containsExactly("fast");
    assertThat(run.tla.getFailedTests())
        .extracting(ITestResult::getName)
        .containsExactly("stubborn");
    assertThat(run.tla.getFailedTests().get(0).getThrowable())
        .isInstanceOf(ThreadTimeoutException.class);
  }

  @Test(description = "GITHUB-3513")
  public void anInterruptibleOverrunIsCountedWhenParallelTestsTimesOut() throws IOException {
    File outputDir = createDirInTempDir("issue3513-interruptible");
    Run run =
        runParallelSuite(outputDir, "slow-test", InterruptibleSample.class, /* interceptor */ null);

    assertThat(run.testng.getStatus()).isEqualTo(ExitCode.FAILED);
    assertThat(run.tla.getPassedTests()).extracting(ITestResult::getName).containsExactly("fast");
    assertThat(run.tla.getFailedTests()).extracting(ITestResult::getName).contains("interruptible");
    assertThat(run.suite.getResults()).containsKeys("slow-test", "fast-test");
    ITestContext slow = run.suite.getResults().get("slow-test").getTestContext();
    assertThat(slow.getFailedTests().size()).isEqualTo(1);
    assertThat(slow.getFailedTests().getAllResults())
        .extracting(ITestResult::getName)
        .contains("interruptible");
    assertReportsContainFailedMethod(outputDir, "interruptible", true);
  }

  @Test(description = "GITHUB-3513")
  public void unfinishedInvocationsAreReportedWhenParallelTestsTimesOut() throws IOException {
    File outputDir = createDirInTempDir("issue3513-invocations");
    Run run =
        runParallelSuite(
            outputDir, "mixed-test", MixedInvocationSample.class, /* interceptor */ null);

    assertThat(run.testng.getStatus()).isEqualTo(ExitCode.FAILED);
    assertThat(run.tla.getPassedTests())
        .extracting(ITestResult::getName)
        .contains("fast", "mixedInvocations");
    List<ITestResult> mixedFailed =
        run.tla.getFailedTests().stream()
            .filter(r -> "mixedInvocations".equals(r.getName()))
            .collect(Collectors.toList());
    assertThat(mixedFailed).hasSize(1);
    assertThat(mixedFailed.get(0).getThrowable()).isInstanceOf(ThreadTimeoutException.class);
    assertReportsContainFailedMethod(outputDir, "mixedInvocations", false);
  }

  @Test(description = "GITHUB-3513")
  public void unfinishedDataProviderRowsAreReportedWhenParallelTestsTimesOut() throws IOException {
    File outputDir = createDirInTempDir("issue3513-dataprovider");
    Run run =
        runParallelSuite(
            outputDir, "mixed-test", MixedDataProviderSample.class, /* interceptor */ null);

    assertThat(run.testng.getStatus()).isEqualTo(ExitCode.FAILED);
    assertThat(run.tla.getPassedTests())
        .extracting(ITestResult::getName)
        .contains("fast", "mixedRows");
    List<ITestResult> mixedFailed =
        run.tla.getFailedTests().stream()
            .filter(r -> "mixedRows".equals(r.getName()))
            .collect(Collectors.toList());
    assertThat(mixedFailed).hasSize(1);
    assertThat(mixedFailed.get(0).getThrowable()).isInstanceOf(ThreadTimeoutException.class);
    assertReportsContainFailedMethod(outputDir, "mixedRows", false);
  }

  @Test(description = "GITHUB-3513")
  public void interceptorDroppedMethodsAreNotReportedAsTimedOut() throws IOException {
    File outputDir = createDirInTempDir("issue3513-interceptor");
    IMethodInterceptor interceptor =
        (List<IMethodInstance> methods, ITestContext context) ->
            methods.stream()
                .filter(m -> !"dropped".equals(m.getMethod().getMethodName()))
                .collect(Collectors.toList());
    Run run = runParallelSuite(outputDir, "stubborn-test", InterceptorSample.class, interceptor);

    assertThat(run.testng.getStatus()).isEqualTo(ExitCode.FAILED);
    assertThat(run.tla.getPassedTests()).extracting(ITestResult::getName).containsExactly("fast");
    assertThat(run.tla.getFailedTests())
        .extracting(ITestResult::getName)
        .containsExactly("stubborn");
    assertThat(run.tla.getFailedTests()).extracting(ITestResult::getName).doesNotContain("dropped");
    assertReportsContainFailedMethod(outputDir, "stubborn", true);
  }

  @Test(description = "GITHUB-3513")
  public void aBlockedAfterMethodDoesNotInventATimeoutFailure() throws InterruptedException {
    File outputDir = createDirInTempDir("issue3513-after-method");
    Run run =
        runParallelSuite(
            outputDir, "after-test", AfterMethodBlockedSample.class, /* interceptor */ null);

    assertThat(run.testng.getStatus()).isEqualTo(0);
    assertThat(run.tla.getPassedTests())
        .extracting(ITestResult::getName)
        .contains("fast", "recorded");
    assertThat(run.tla.getFailedTests()).isEmpty();

    Thread.sleep(2_500);
    assertThat(run.testng.getStatus()).isEqualTo(0);
    assertThat(run.tla.getPassedTests())
        .extracting(ITestResult::getName)
        .contains("fast", "recorded");
    assertThat(run.tla.getFailedTests()).isEmpty();
  }

  /**
   * A listener admitted before the freeze may finish after it. Timeout finalization must not wait
   * for that callback.
   */
  @Test(description = "GITHUB-3513")
  public void aBlockingListenerDoesNotDelayTimeoutFinalization() throws Exception {
    File outputDir = createDirInTempDir("issue3513-blocking-listener");
    XmlSuite suite = createXmlSuite("issue3513");
    suite.setParallel(XmlSuite.ParallelMode.TESTS);
    suite.setThreadCount(2);
    suite.setTimeOut(Long.toString(SUITE_TIME_OUT_MILLIS));
    createXmlTest(suite, "stubborn-test", StubbornSample.class);
    createXmlTest(suite, "fast-test", FastSample.class);

    TestNG testng = create(suite);
    testng.setUseDefaultListeners(true);
    testng.setOutputDirectory(outputDir.getAbsolutePath());
    BlockingStartListener blocking = new BlockingStartListener();
    TimeoutFailureGate gate = new TimeoutFailureGate();
    testng.addListener((ITestNGListener) blocking);
    testng.addListener((ITestNGListener) gate);

    Thread runner = new Thread(testng::run, "issue3513-blocking-listener");
    runner.start();
    try {
      assertThat(blocking.entered.await(5, TimeUnit.SECONDS)).isTrue();
      assertThat(gate.stubbornFailed.await(2, TimeUnit.SECONDS)).isTrue();
      assertThat(gate.stubborn.getThrowable()).isInstanceOf(ThreadTimeoutException.class);
    } finally {
      blocking.release.countDown();
      runner.join(10_000);
    }
  }

  /**
   * A terminal listener admitted before freeze may finish after it. The same invocation must not
   * also get a synthetic timeout failure.
   */
  @Test(description = "GITHUB-3513")
  public void aBlockingOnTestSuccessDoesNotAlsoPublishATimeoutFailure() throws Exception {
    File outputDir = createDirInTempDir("issue3513-blocking-success");
    XmlSuite suite = createXmlSuite("issue3513");
    suite.setParallel(XmlSuite.ParallelMode.TESTS);
    suite.setThreadCount(2);
    suite.setTimeOut(Long.toString(SUITE_TIME_OUT_MILLIS));
    createXmlTest(suite, "fast-test", FastSample.class);
    createXmlTest(suite, "stubborn-test", StubbornSample.class);

    TestNG testng = create(suite);
    testng.setUseDefaultListeners(true);
    testng.setOutputDirectory(outputDir.getAbsolutePath());
    BlockingSuccessListener blocking = new BlockingSuccessListener();
    TimeoutFailureGate gate = new TimeoutFailureGate();
    TestListenerAdapter tla = new TestListenerAdapter();
    testng.addListener((ITestNGListener) blocking);
    testng.addListener((ITestNGListener) gate);
    testng.addListener((ITestNGListener) tla);

    Thread runner = new Thread(testng::run, "issue3513-blocking-success");
    runner.start();
    try {
      assertThat(blocking.entered.await(5, TimeUnit.SECONDS)).isTrue();
      assertThat(gate.stubbornFailed.await(2, TimeUnit.SECONDS)).isTrue();
      assertThat(blocking.statuses).containsExactly(ITestResult.SUCCESS);
    } finally {
      blocking.release.countDown();
      runner.join(10_000);
    }

    assertThat(blocking.statuses).containsExactly(ITestResult.SUCCESS);
    assertThat(tla.getPassedTests()).extracting(ITestResult::getName).contains("fast");
    assertThat(tla.getFailedTests()).extracting(ITestResult::getName).doesNotContain("fast");
  }

  @Test
  public void threadUtilExecuteKeepsVoidReturnType() throws NoSuchMethodException {
    assertThat(
            ThreadUtil.class
                .getMethod(
                    "execute",
                    IConfiguration.class,
                    String.class,
                    List.class,
                    int.class,
                    long.class)
                .getReturnType())
        .isEqualTo(void.class);
  }

  private static Run runParallelSuite(
      File outputDir, String slowTestName, Class<?> slowSample, IMethodInterceptor interceptor) {
    XmlSuite suite = createXmlSuite("issue3513");
    suite.setParallel(XmlSuite.ParallelMode.TESTS);
    suite.setThreadCount(2);
    suite.setTimeOut(Long.toString(SUITE_TIME_OUT_MILLIS));
    createXmlTest(suite, slowTestName, slowSample);
    createXmlTest(suite, "fast-test", FastSample.class);

    TestNG testng = create(suite);
    testng.setUseDefaultListeners(true);
    testng.setOutputDirectory(outputDir.getAbsolutePath());
    if (interceptor != null) {
      testng.setMethodInterceptor(interceptor);
    }
    TestListenerAdapter tla = new TestListenerAdapter();
    testng.addListener((ITestNGListener) tla);
    SuiteHolder holder = new SuiteHolder();
    testng.addListener((ITestNGListener) holder);
    testng.run();
    return new Run(testng, tla, holder.suite);
  }

  private static void assertReportsContainFailedMethod(
      File outputDir, String methodName, boolean failedXml) throws IOException {
    File results = new File(outputDir, RuntimeBehavior.FILE_NAME);
    assertThat(results).exists();
    assertThat(Files.readString(results.toPath()))
        .contains("name=\"" + methodName + "\"")
        .contains("status=\"FAIL\"");
    if (failedXml) {
      File failed = new File(outputDir, FailedReporter.TESTNG_FAILED_XML);
      assertThat(failed).exists();
      assertThat(Files.readString(failed.toPath())).contains(methodName);
    }
  }

  private static final class Run {
    private final TestNG testng;
    private final TestListenerAdapter tla;
    private final ISuite suite;

    private Run(TestNG testng, TestListenerAdapter tla, ISuite suite) {
      this.testng = testng;
      this.tla = tla;
      this.suite = suite;
    }
  }

  private static final class SuiteHolder implements ISuiteListener {
    private ISuite suite;

    @Override
    public void onFinish(ISuite suite) {
      this.suite = suite;
    }
  }

  /**
   * Parks in {@code onTestSuccess} across the suite time-out. Records every terminal outcome for
   * {@code fast}.
   */
  private static final class BlockingSuccessListener implements ITestListener {
    private final CountDownLatch entered = new CountDownLatch(1);
    private final CountDownLatch release = new CountDownLatch(1);
    private final List<Integer> statuses = new CopyOnWriteArrayList<>();

    @Override
    public void onTestSuccess(ITestResult result) {
      if (!"fast".equals(result.getName())) {
        return;
      }
      statuses.add(result.getStatus());
      entered.countDown();
      long end = System.currentTimeMillis() + 10_000;
      while (release.getCount() > 0 && System.currentTimeMillis() < end) {
        try {
          release.await(50, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {
          // Keep the callback open across suite cancellation.
        }
      }
    }

    @Override
    public void onTestFailure(ITestResult result) {
      if (!"fast".equals(result.getName())) {
        return;
      }
      statuses.add(result.getStatus());
    }
  }

  /** Parks in {@code onTestStart} so timeout finalization cannot wait on this callback. */
  private static final class BlockingStartListener implements ITestListener {
    private final CountDownLatch entered = new CountDownLatch(1);
    private final CountDownLatch release = new CountDownLatch(1);

    @Override
    public void onTestStart(ITestResult result) {
      if (!"stubborn".equals(result.getName())) {
        return;
      }
      entered.countDown();
      try {
        release.await(10, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  private static final class TimeoutFailureGate implements ITestListener {
    private final CountDownLatch stubbornFailed = new CountDownLatch(1);
    private volatile ITestResult stubborn;

    @Override
    public void onTestFailure(ITestResult result) {
      if (!"stubborn".equals(result.getName())) {
        return;
      }
      stubborn = result;
      stubbornFailed.countDown();
    }
  }
}
