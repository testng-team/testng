package test.invocationcount;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.reporters.snapshot.SkippedInvocationCountSample;
import org.testng.reporters.snapshot.SkippedParallelInvocationCountSample;
import test.SimpleBaseTest;

/**
 * What an invocation cancelled by a failing one is reported as, for a sequential data provider and
 * for a parallel one.
 *
 * <p>Each pair of samples differs in nothing but {@code parallel = true}, so anything the two
 * shapes do not report identically is a divergence between the two cancellation loops -- {@code
 * MethodRunner#runInSequence} and {@code TestMethodWithDataProviderMethodWorker}. The tests are
 * written as that comparison rather than as two sets of literals, so a divergence cannot be
 * recorded as an expectation by accident.
 *
 * <p>Three things are recorded, because different consumers are told by different means:
 *
 * <ul>
 *   <li>what an {@link ITestListener} is told, which is what a console or CI listener reacts to;
 *   <li>what {@link ITestContext} holds once the test tag has finished, which is what the built-in
 *       reporters -- {@code testng-results.xml} above all -- are generated from;
 *   <li>the values each skipped result carries, which is what a reporter prints for it.
 * </ul>
 */
public class CancelledInvocationReportingTest extends SimpleBaseTest {

  @Test(
      description =
          "GITHUB-3408: whether the data provider was parallel is not something a consumer of the"
              + " results can tell")
  public void cancellationIsReportedTheSameWayWhateverTheDataProvider() {
    assertThat(report(SkippedParallelInvocationCountSample.class))
        .isEqualTo(report(SkippedInvocationCountSample.class))
        .containsExactly(
            // One invocation ran and failed; the two still to come were cancelled ...
            "listener: failed=1 skipped=2",
            // ... and the context was told about the same three.
            "context: failed=1 skipped=2",
            // All three were announced as starting, cancelled ones included, each carrying the row
            // it would have re-run.
            "started: [[only-row], [only-row], [only-row]]",
            "skipped values: [[only-row], [only-row]]");
  }

  @Test(
      description =
          "GITHUB-3408: cancelling stops the invocationCounts still to come, not the rows of the"
              + " repetition under way")
  public void everyRowStillRunsWhenOneOfThemCancels() {
    // An invocationCount counts repetitions of the whole data set, so a row failing says nothing
    // about its siblings: runInSequence goes on iterating them after it has cancelled. The parallel
    // shape must not do otherwise -- claiming the counter before invoking, rather than after
    // failing, would let one row silently swallow the rest.
    assertThat(rowReport(CancelledParallelRows.class))
        .isEqualTo(rowReport(CancelledSequentialRows.class))
        .containsExactly("ran: [[fails-a], [fails-b], [passes]]", "failed=2 passed=1 skipped=1");
  }

  /**
   * Runs the sample and returns what each consumer saw, one line each, so that a difference between
   * the two shapes reads as a diff rather than as a set of unrelated numbers.
   */
  private static List<String> report(Class<?> sample) {
    Recorder recorder = run(sample);

    return Arrays.asList(
        "listener: failed=" + recorder.failed.size() + " skipped=" + recorder.skippedValues.size(),
        "context: failed=" + recorder.contextFailed + " skipped=" + recorder.contextSkipped,
        "started: " + recorder.started,
        "skipped values: " + recorder.skippedValues);
  }

  /**
   * The same for a sample with several rows, where which row wins the race to cancel -- and so
   * which row the skip is reported with -- is not determined. Only what is: that every row ran, and
   * how many results of each kind came out.
   */
  private static List<String> rowReport(Class<?> sample) {
    Recorder recorder = run(sample);

    List<String> ran = new ArrayList<>(recorder.failed);
    ran.addAll(recorder.succeeded);
    Collections.sort(ran);
    return Arrays.asList(
        "ran: " + ran,
        "failed="
            + recorder.failed.size()
            + " passed="
            + recorder.succeeded.size()
            + " skipped="
            + recorder.skippedValues.size());
  }

  private static Recorder run(Class<?> sample) {
    TestNG testng = create(sample);
    Recorder recorder = new Recorder();
    testng.addListener(recorder);
    testng.run();
    return recorder;
  }

  /**
   * What the listeners were told, and what the context held once it was over.
   *
   * <p>A parallel data provider announces from a worker thread, so the lists are synchronized.
   */
  private static class Recorder implements ITestListener {

    private final List<String> started = Collections.synchronizedList(new ArrayList<>());
    private final List<String> skippedValues = Collections.synchronizedList(new ArrayList<>());
    private final List<String> failed = Collections.synchronizedList(new ArrayList<>());
    private final List<String> succeeded = Collections.synchronizedList(new ArrayList<>());
    private int contextFailed;
    private int contextSkipped;

    @Override
    public void onTestStart(ITestResult result) {
      started.add(Arrays.toString(result.getParameters()));
    }

    @Override
    public void onTestSkipped(ITestResult result) {
      skippedValues.add(Arrays.toString(result.getParameters()));
    }

    @Override
    public void onTestFailure(ITestResult result) {
      failed.add(Arrays.toString(result.getParameters()));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
      succeeded.add(Arrays.toString(result.getParameters()));
    }

    @Override
    public void onFinish(ITestContext context) {
      contextFailed = context.getFailedTests().size();
      contextSkipped = context.getSkippedTests().size();
    }
  }
}
