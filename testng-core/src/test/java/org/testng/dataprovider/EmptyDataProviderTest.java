package org.testng.dataprovider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.testng.EmptyDataProviderBehavior;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.dataprovider.samples.DataProviderInvocationCountSample;
import org.testng.dataprovider.samples.DataProviderInvocationCountThreadPoolSample;
import org.testng.dataprovider.samples.DryRunEmptyDataProviderSample;
import org.testng.dataprovider.samples.EmptyDataProviderConfigurationSample;
import org.testng.dataprovider.samples.EmptyDataProviderConfigurationThreadPoolSample;
import org.testng.dataprovider.samples.EmptyDataProviderDependencySample;
import org.testng.dataprovider.samples.EmptyDataProviderFactorySample;
import org.testng.dataprovider.samples.EmptyDataProviderFactoryThreadPoolSample;
import org.testng.dataprovider.samples.EmptyDataProviderInvocationCountSample;
import org.testng.dataprovider.samples.EmptyDataProviderParallelSample;
import org.testng.dataprovider.samples.EmptyDataProviderThreadPoolSample;
import org.testng.dataprovider.samples.EmptyIteratorDataProviderSample;
import org.testng.dataprovider.samples.EmptyStreamCloseDataProviderSample;
import org.testng.dataprovider.samples.EmptyStreamCloseThreadPoolDataProviderSample;
import org.testng.dataprovider.samples.FilteredOutDataProviderDependencySample;
import org.testng.dataprovider.samples.FilteredOutDataProviderSample;
import org.testng.dataprovider.samples.PartiallyEmptyDataProviderSample;
import org.testng.dataprovider.samples.PartiallyEmptyDataProviderThreadPoolSample;
import org.testng.dataprovider.samples.ThrowingHasNextDataProviderSample;
import org.testng.dataprovider.samples.ThrowingHasNextDependencySample;
import org.testng.internal.ExitCode;
import org.testng.internal.RuntimeBehavior;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;
import test.dataprovider.EmptyDataProviderSample;
import test.dataprovider.issue3290.EmptyStreamDataProviderSample;

/**
 * A test method whose data provider hands out no row used to produce no {@code ITestResult} at all,
 * so it vanished from the reporters. It now yields exactly one skipped result, on every execution
 * path, without the data provider being evaluated any more often than before.
 */
public class EmptyDataProviderTest extends SimpleBaseTest {

  @Test(description = "GITHUB-3478")
  public void aDataProviderThatRunsOutOfRowsKeepsTheResultsOfTheInvocationsThatGotSome() {
    PartiallyEmptyDataProviderThreadPoolSample.CALLS.set(0);

    InvokedMethodNameListener listener = run(PartiallyEmptyDataProviderThreadPoolSample.class);

    // Two of the three pooled invocations got a row. The method has something to show for itself,
    // so there is no empty-data-provider skip to report and their results must survive.
    assertThat(listener.getFailedMethodNames()).isEmpty();
    assertThat(listener.getSucceedMethodNames()).hasSize(2);
    assertThat(listener.getSkippedMethodNames()).isEmpty();
  }

  @Test(description = "GITHUB-3478")
  public void anEmptyInvocationDoesNotCancelTheInvocationsAfterIt() {
    PartiallyEmptyDataProviderSample.CALLS.set(0);
    PartiallyEmptyDataProviderSample.BODIES.set(0);

    InvokedMethodNameListener listener = run(PartiallyEmptyDataProviderSample.class);

    // invocationCount = 5 with the data provider empty on the third call only. The empty call
    // produces nothing; it must not end the invocation loop.
    assertThat(PartiallyEmptyDataProviderSample.CALLS).hasValue(5);
    assertThat(PartiallyEmptyDataProviderSample.BODIES).hasValue(4);
    assertThat(listener.getSucceedMethodNames()).hasSize(4);
    assertThat(listener.getSkippedMethodNames()).isEmpty();
  }

  @Test(description = "GITHUB-3478")
  public void aDataProviderThrowingFromHasNextFailsTheMethodRatherThanTheRun() {
    TestNG tng = create(ThrowingHasNextDataProviderSample.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);

    tng.run();

    // Asking the iterator whether it is empty must not escape the invocation: the throw belongs to
    // the method that owns the data provider, and the rest of the class still runs.
    assertThat(tla.getFailedTests()).extracting(ITestResult::getName).containsExactly("first");
    assertThat(tla.getPassedTests()).extracting(ITestResult::getName).containsExactly("second");
    assertThat(tng.getStatus()).isEqualTo(ExitCode.FAILED);
  }

  @Test(description = "GITHUB-3478")
  public void aDataProviderThrowingFromHasNextIsStillClosedWhenTheMethodIsAlreadySkipped() {
    ThrowingHasNextDependencySample.CLOSE_COUNT.set(0);
    TestNG tng = create(ThrowingHasNextDependencySample.class);
    tng.setReportAllDataDrivenTestsAsSkipped(true);

    assertThatThrownBy(tng::run).isInstanceOf(IllegalStateException.class);

    // Whatever the throw does to the run, the stream behind the data provider is released.
    assertThat(ThrowingHasNextDependencySample.CLOSE_COUNT).hasValue(1);
  }

  @Test(description = "GITHUB-3478")
  public void emptyDataProviderIgnoreTest() {
    TestNG tng = create(EmptyDataProviderSample.class);
    tng.setEmptyDataProviderBehavior(EmptyDataProviderBehavior.IGNORE);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);
    tng.run();

    assertThat(listener.getFailedMethodNames()).isEmpty();
    assertThat(listener.getSkippedMethodNames()).isEmpty();
    assertThat(listener.getSucceedMethodNames()).isEmpty();
    assertThat(listener.getInvokedMethodNames()).isEmpty();
  }

  @Test(description = "GITHUB-3478")
  public void emptyIteratorDataProviderTest() {
    InvokedMethodNameListener listener = run(EmptyIteratorDataProviderSample.class);

    assertThat(listener.getFailedMethodNames()).isEmpty();
    assertThat(listener.getSkippedMethodNames()).containsExactly("test");
    assertThat(listener.getInvokedMethodNames()).isEmpty();
    assertThat(listener.getResult("test").getStatus()).isEqualTo(ITestResult.SKIP);
  }

  @Test(description = "GITHUB-3290")
  public void emptyStreamDataProviderWithIgnoreBehaviorShouldResultInNoInvocations() {
    TestNG tng = create(EmptyStreamDataProviderSample.class);
    tng.setEmptyDataProviderBehavior(EmptyDataProviderBehavior.IGNORE);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);
    tng.run();

    assertThat(listener.getFailedMethodNames()).isEmpty();
    assertThat(listener.getSkippedMethodNames()).isEmpty();
    assertThat(listener.getSucceedMethodNames()).isEmpty();
    assertThat(listener.getInvokedMethodNames()).isEmpty();
  }

  @Test(description = "GITHUB-3478")
  public void emptyStreamDataProviderClosesStreamExactlyOnce() {
    EmptyStreamCloseDataProviderSample.CLOSE_COUNT.set(0);
    InvokedMethodNameListener listener = run(EmptyStreamCloseDataProviderSample.class);

    assertThat(listener.getFailedMethodNames()).isEmpty();
    assertThat(listener.getSkippedMethodNames()).containsExactly("test");
    assertThat(listener.getSucceedMethodNames()).isEmpty();
    assertThat(listener.getInvokedMethodNames()).isEmpty();
    assertThat(EmptyStreamCloseDataProviderSample.CLOSE_COUNT.get()).isEqualTo(1);
  }

  @Test(description = "GITHUB-3478")
  public void emptyParallelDataProviderTest() {
    InvokedMethodNameListener listener = run(EmptyDataProviderParallelSample.class);

    assertThat(listener.getFailedMethodNames()).isEmpty();
    assertThat(listener.getSkippedMethodNames()).containsExactly("testParallel");
    assertThat(listener.getSucceedMethodNames()).isEmpty();
    assertThat(listener.getInvokedMethodNames()).isEmpty();
    assertThat(listener.getResult("testParallel").getParameters()).isEmpty();
    assertThat(listener.getResult("testParallel").getStatus()).isEqualTo(ITestResult.SKIP);
  }

  @Test(description = "GITHUB-3478")
  public void emptyParallelDataProviderIgnoreTest() {
    TestNG tng = create(EmptyDataProviderParallelSample.class);
    tng.setEmptyDataProviderBehavior(EmptyDataProviderBehavior.IGNORE);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);
    tng.run();

    assertThat(listener.getFailedMethodNames()).isEmpty();
    assertThat(listener.getSkippedMethodNames()).isEmpty();
    assertThat(listener.getSucceedMethodNames()).isEmpty();
    assertThat(listener.getInvokedMethodNames()).isEmpty();
  }

  @Test(description = "GITHUB-3478")
  public void emptyDataProviderInvocationCountTest() {
    InvokedMethodNameListener listener = run(EmptyDataProviderInvocationCountSample.class);

    assertThat(listener.getFailedMethodNames()).isEmpty();
    assertThat(listener.getSkippedMethodNames()).containsExactly("testMultipleInvocations");
    assertThat(listener.getSucceedMethodNames()).isEmpty();
    assertThat(listener.getInvokedMethodNames()).isEmpty();
    assertThat(listener.getResult("testMultipleInvocations").getParameters()).isEmpty();
    assertThat(listener.getResult("testMultipleInvocations").getStatus())
        .isEqualTo(ITestResult.SKIP);
  }

  @Test(description = "GITHUB-3478")
  public void emptyDataProviderThreadPoolTest() {
    EmptyDataProviderThreadPoolSample.DATA_PROVIDER_INVOCATIONS.set(0);

    InvokedMethodNameListener listener = run(EmptyDataProviderThreadPoolSample.class);

    assertThat(listener.getFailedMethodNames()).isEmpty();
    assertThat(listener.getSkippedMethodNames()).containsExactly("testPooledInvocations");
    assertThat(listener.getSucceedMethodNames()).isEmpty();
    assertThat(listener.getInvokedMethodNames()).isEmpty();
    assertThat(listener.getResult("testPooledInvocations").getParameters()).isEmpty();
    assertThat(listener.getResult("testPooledInvocations").getStatus()).isEqualTo(ITestResult.SKIP);
    // Each invocation of the pool evaluates the data provider once, as it does without a pool.
    // Deciding that it is empty must not cost one more evaluation on top of that.
    assertThat(EmptyDataProviderThreadPoolSample.DATA_PROVIDER_INVOCATIONS).hasValue(3);
  }

  @Test(description = "GITHUB-3478")
  public void emptyDataProviderThreadPoolIgnoreTest() {
    TestNG tng = create(EmptyDataProviderThreadPoolSample.class);
    tng.setEmptyDataProviderBehavior(EmptyDataProviderBehavior.IGNORE);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);
    tng.run();

    assertThat(listener.getFailedMethodNames()).isEmpty();
    assertThat(listener.getSkippedMethodNames()).isEmpty();
    assertThat(listener.getSucceedMethodNames()).isEmpty();
    assertThat(listener.getInvokedMethodNames()).isEmpty();
  }

  @Test(description = "GITHUB-3478")
  public void emptyStreamDataProviderIsClosedOncePerPooledInvocation() {
    EmptyStreamCloseThreadPoolDataProviderSample.OPEN_COUNT.set(0);
    EmptyStreamCloseThreadPoolDataProviderSample.CLOSE_COUNT.set(0);

    InvokedMethodNameListener listener = run(EmptyStreamCloseThreadPoolDataProviderSample.class);

    assertThat(listener.getSkippedMethodNames()).containsExactly("test");
    // One stream per invocation, as without the pool, and every one of them released.
    assertThat(EmptyStreamCloseThreadPoolDataProviderSample.OPEN_COUNT).hasValue(3);
    assertThat(EmptyStreamCloseThreadPoolDataProviderSample.CLOSE_COUNT)
        .hasValue(EmptyStreamCloseThreadPoolDataProviderSample.OPEN_COUNT.get());
  }

  @Test(description = "GITHUB-3478")
  public void pooledInvocationsDoNotEvaluateTheDataProviderMoreOften() {
    DataProviderInvocationCountThreadPoolSample.DATA_PROVIDER_INVOCATIONS.set(0);
    DataProviderInvocationCountSample.DATA_PROVIDER_INVOCATIONS.set(0);

    InvokedMethodNameListener pooled = run(DataProviderInvocationCountThreadPoolSample.class);
    InvokedMethodNameListener sequential = run(DataProviderInvocationCountSample.class);

    assertThat(pooled.getFailedMethodNames()).isEmpty();
    assertThat(sequential.getFailedMethodNames()).isEmpty();
    assertThat(pooled.getSucceedMethodNames()).hasSameSizeAs(sequential.getSucceedMethodNames());
    // The two samples differ only by threadPoolSize. Running the invocations in a pool must not
    // evaluate the data provider more often than running them sequentially does.
    assertThat(DataProviderInvocationCountThreadPoolSample.DATA_PROVIDER_INVOCATIONS)
        .hasValue(DataProviderInvocationCountSample.DATA_PROVIDER_INVOCATIONS.get());
  }

  @Test(description = "GITHUB-3478")
  public void filteredOutDataProviderIsNotTreatedAsEmpty() {
    InvokedMethodNameListener listener = run(FilteredOutDataProviderSample.class);

    // The data provider is not empty, its rows are merely excluded by indices, so neither method
    // produces a result. The pooled one must not be told apart from the sequential one just
    // because both happen to produce nothing.
    assertThat(listener.getFailedMethodNames()).isEmpty();
    assertThat(listener.getSkippedMethodNames()).isEmpty();
    assertThat(listener.getSucceedMethodNames()).isEmpty();
    assertThat(listener.getInvokedMethodNames()).isEmpty();
  }

  @Test(description = "GITHUB-3478")
  public void filteredOutDataProviderIsNotTreatedAsEmptyWhenTheMethodIsAlreadySkipped() {
    TestNG tng = create(FilteredOutDataProviderDependencySample.class);
    tng.setReportAllDataDrivenTestsAsSkipped(true);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);
    tng.run();

    assertThat(listener.getFailedMethodNames()).containsExactly("failing");
    // reportAllDataDrivenTestsAsSkipped announces one skipped result per row the method would have
    // run. Every row is excluded here, so there is none to announce - and the provider is not
    // empty, so there is no empty-data-provider skip to announce either.
    assertThat(listener.getSkippedMethodNames()).isEmpty();
  }

  @Test(description = "GITHUB-3478")
  public void emptyDataProviderRunsNoConfigurationMethod() {
    EmptyDataProviderConfigurationSample.BEFORE.set(0);
    EmptyDataProviderConfigurationSample.AFTER.set(0);
    EmptyDataProviderConfigurationSample.FIRST_TIME_ONLY.set(0);

    InvokedMethodNameListener listener = run(EmptyDataProviderConfigurationSample.class);

    assertThat(listener.getSkippedMethodNames()).containsExactly("sequential");
    // The skipped result stands for invocations that never happened, so nothing that surrounds an
    // invocation may run because of it.
    assertThat(EmptyDataProviderConfigurationSample.BEFORE).hasValue(0);
    assertThat(EmptyDataProviderConfigurationSample.AFTER).hasValue(0);
    assertThat(EmptyDataProviderConfigurationSample.FIRST_TIME_ONLY).hasValue(0);
  }

  @Test(description = "GITHUB-3478")
  public void emptyDataProviderRunsOnlyTheConfigurationsThatSurroundThePool() {
    EmptyDataProviderConfigurationThreadPoolSample.BEFORE.set(0);
    EmptyDataProviderConfigurationThreadPoolSample.AFTER.set(0);
    EmptyDataProviderConfigurationThreadPoolSample.FIRST_TIME_ONLY.set(0);

    InvokedMethodNameListener listener = run(EmptyDataProviderConfigurationThreadPoolSample.class);

    assertThat(listener.getSkippedMethodNames()).containsExactly("pooled");
    assertThat(EmptyDataProviderConfigurationThreadPoolSample.BEFORE).hasValue(0);
    assertThat(EmptyDataProviderConfigurationThreadPoolSample.AFTER).hasValue(0);
    // A firstTimeOnly @BeforeMethod is a barrier for a parallel invocationCount, so it runs around
    // the thread pool (GITHUB-426) and not inside an invocation. It is therefore not conditioned on
    // what the data provider returns - which is the behavior without this feature too.
    assertThat(EmptyDataProviderConfigurationThreadPoolSample.FIRST_TIME_ONLY).hasValue(1);
  }

  @Test(description = "GITHUB-3478")
  public void emptyDataProviderSkipsTheMethodsDependingOnIt() {
    InvokedMethodNameListener listener = run(EmptyDataProviderDependencySample.class);

    assertThat(listener.getFailedMethodNames()).isEmpty();
    // The skipped result the empty data provider produces is a registered skip, so it propagates
    // along the dependency graph like any other.
    assertThat(listener.getSkippedMethodNames()).containsExactlyInAnyOrder("producer", "consumer");
    assertThat(listener.getSucceedMethodNames()).isEmpty();
  }

  @Test(description = "GITHUB-3478")
  public void emptyDataProviderWithIgnoreBehaviorLeavesDependentMethodsRunning() {
    TestNG tng = create(EmptyDataProviderDependencySample.class);
    tng.setEmptyDataProviderBehavior(EmptyDataProviderBehavior.IGNORE);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);
    tng.run();

    assertThat(listener.getFailedMethodNames()).isEmpty();
    assertThat(listener.getSkippedMethodNames()).isEmpty();
    assertThat(listener.getSucceedMethodNames()).containsExactly("consumer");
  }

  @Test(description = "GITHUB-3478")
  public void emptyDataProviderReportsOneSkipPerFactoryInstance() {
    InvokedMethodNameListener listener = run(EmptyDataProviderFactorySample.class);

    assertThat(listener.getFailedMethodNames()).isEmpty();
    assertThat(listener.getInvokedMethodNames()).isEmpty();
    // The @Factory produces two instances, and the skipped result stands for the test method of
    // one instance - not for the method as declared.
    assertThat(listener.getSkippedMethodNames()).containsExactly("test", "test");
  }

  @Test(description = "GITHUB-3478")
  public void emptyDataProviderReportsOneSkipPerFactoryInstanceWhenPooled() {
    TestNG tng = create(EmptyDataProviderFactoryThreadPoolSample.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);

    tng.run();

    assertThat(tla.getPassedTests()).isEmpty();
    assertThat(tla.getFailedTests()).isEmpty();
    // invokePooledTestMethods runs once per factory instance and clones the method for each
    // invocation, but the skipped result it reports is the instance's own: one per instance,
    // carrying that instance rather than a clone shared with the other one.
    assertThat(tla.getSkippedTests())
        .extracting(r -> ((EmptyDataProviderFactoryThreadPoolSample) r.getInstance()).name())
        .containsExactlyInAnyOrder("first", "second");
    assertThat(tla.getSkippedTests()).allSatisfy(r -> assertThat(r.getParameters()).isEmpty());
  }

  @Test(description = "GITHUB-3478")
  public void emptyDataProviderIsReportedInTheExitStatus() {
    TestNG skipping = create(EmptyDataProviderSample.class);
    skipping.run();

    TestNG ignoring = create(EmptyDataProviderSample.class);
    ignoring.setEmptyDataProviderBehavior(EmptyDataProviderBehavior.IGNORE);
    ignoring.run();

    // A suite whose only methods are backed by an empty data provider used to run no test at all;
    // it now reports the skips, which is what a build reading the exit code needs to see.
    assertThat(skipping.getStatus()).isEqualTo(ExitCode.SKIPPED);
    assertThat(ignoring.getStatus()).isEqualTo(ExitCode.HAS_NO_TEST);
  }

  @Test(description = "junit-team/testng-engine#422")
  public void dryRunEmptyDataProviderTest() {
    System.setProperty(RuntimeBehavior.TESTNG_MODE_DRYRUN, "true");
    try {
      InvokedMethodNameListener listener = run(true, DryRunEmptyDataProviderSample.class);

      assertThat(listener.getFailedMethodNames()).isEmpty();
      assertThat(listener.getSkippedMethodNames()).containsExactly("testMethod");
      assertThat(listener.getSucceedMethodNames()).isEmpty();
      assertThat(listener.getInvokedMethodNames()).isEmpty();
      assertThat(listener.getResult("testMethod").getParameters()).isEmpty();
      assertThat(listener.getResult("testMethod").getStatus()).isEqualTo(ITestResult.SKIP);
    } finally {
      System.setProperty(RuntimeBehavior.TESTNG_MODE_DRYRUN, "false");
    }
  }
}
