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
 * <p>The two samples differ in nothing but {@code parallel = true}, so every line below that is not
 * identical between the two shapes is a divergence between the two cancellation loops -- {@code
 * MethodRunner#runInSequence} and {@code TestMethodWithDataProviderMethodWorker}.
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

  @Test(description = "GITHUB-3408: a cancelled invocation of a sequential data provider")
  public void sequentialCancellationIsReportedAs() {
    assertThat(report(SkippedInvocationCountSample.class))
        .containsExactly(
            // One invocation ran and failed; the two still to come were cancelled.
            "listener: failed=1 skipped=2",
            // ... and the context is told about the same three.
            "context: failed=1 skipped=2",
            "started: [[only-row], [only-row], [only-row]]",
            "skipped values: [[only-row], [only-row]]");
  }

  @Test(description = "GITHUB-3408: a cancelled invocation of a parallel data provider")
  public void parallelCancellationIsReportedAs() {
    assertThat(report(SkippedParallelInvocationCountSample.class))
        .containsExactly(
            // Three invocations ran and failed, not one: cancelling does not stop the outer
            // invocationCount loop, so the data provider is run again for each count left.
            "listener: failed=3 skipped=3",
            // The context does hear about the cancelled ones here.
            "context: failed=3 skipped=3",
            // Only the three that ran were announced as starting. The cancelled ones go straight
            // to skipped, so a listener capturing onTestStart never sees them ...
            "started: [[only-row], [only-row], [only-row]]",
            // ... and they are reported with no values, though the row they would have re-run is
            // a field of the worker that cancelled them.
            "skipped values: [[], [], []]");
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
    public void onFinish(ITestContext context) {
      contextFailed = context.getFailedTests().size();
      contextSkipped = context.getSkippedTests().size();
    }
  }
}
