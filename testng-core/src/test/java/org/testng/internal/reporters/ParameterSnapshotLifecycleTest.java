package org.testng.internal.reporters;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.Nullable;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.reporters.snapshot.NonCloneableParameterSample;
import org.testng.reporters.snapshot.RenderingSample;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

/**
 * Who owns the reporting snapshots and for how long. The {@link Probe} stands in for a built-in
 * reporter: it asks for the snapshots when a context starts, and looks them up again from {@link
 * IReporter#generateReport}, which TestNG runs once every context has finished.
 */
public class ParameterSnapshotLifecycleTest extends SimpleBaseTest {

  @Test(description = "A finished ITestContext does not take the snapshots reporting still needs")
  public void snapshotsOutliveTheContextsThatFilledThem() {
    Probe probe = new Probe();

    runTwoContextsUnder(probe);

    assertThat(probe.results).hasSize(4);
    assertThat(probe.resultsMissingWhenTheReportersRan).isEmpty();
  }

  @Test(description = "A completed run leaves no reporting snapshot state behind it")
  public void aCompletedRunReleasesItsSnapshots() {
    Probe probe = new Probe();

    runTwoContextsUnder(probe);

    assertThat(ParameterSnapshots.of(probe.suite)).isNull();
    assertThat(requireNonNull(probe.snapshots).isEmpty()).isTrue();
  }

  @Test(description = "A run cannot observe the snapshots of the one before it")
  public void separateRunsDoNotShareSnapshots() {
    Probe first = new Probe();
    runTwoContextsUnder(first);

    Probe second = new Probe();
    second.foreignResults.addAll(first.results);
    runTwoContextsUnder(second);

    assertThat(second.snapshots).isNotSameAs(first.snapshots);
    assertThat(second.suite).isNotSameAs(first.suite);
    // Not one of the first run's results is in the second run's store.
    assertThat(second.foreignResultsMissingWhenTheReportersRan)
        .containsExactlyElementsOf(first.results);
  }

  @Test(description = "Two TestNG executions in flight together own separate snapshots")
  public void concurrentRunsDoNotShareSnapshots() throws Exception {
    Probe first = new Probe();
    Probe second = new Probe();

    ExecutorService pool = Executors.newFixedThreadPool(2);
    try {
      List<Future<Void>> runs =
          pool.invokeAll(List.of(runningUnder(first), runningUnder(second)), 2, TimeUnit.MINUTES);
      for (Future<Void> run : runs) {
        run.get();
      }
    } finally {
      pool.shutdownNow();
    }

    assertThat(first.snapshots).isNotNull();
    assertThat(second.snapshots).isNotSameAs(first.snapshots);
    assertThat(first.resultsMissingWhenTheReportersRan).isEmpty();
    assertThat(second.resultsMissingWhenTheReportersRan).isEmpty();
  }

  private static Callable<Void> runningUnder(Probe probe) {
    return () -> {
      runTwoContextsUnder(probe);
      return null;
    };
  }

  /** One {@code <test>} per sample, so a context finishes while the other still has snapshots. */
  private static void runTwoContextsUnder(Probe probe) {
    TestNG testng =
        createTests("snapshot-lifecycle", RenderingSample.class, NonCloneableParameterSample.class);
    testng.addListener(probe);
    testng.run();
  }

  /**
   * A consumer of the shared snapshots, of the kind this infrastructure exists for: it declares its
   * interest once per context and reads the store back after every context is done.
   */
  private static final class Probe implements ITestListener, IReporter {

    final List<ITestResult> results = new ArrayList<>();
    final List<ITestResult> foreignResults = new ArrayList<>();

    volatile @Nullable ISuite suite;
    volatile @Nullable ParameterSnapshots snapshots;
    volatile List<ITestResult> resultsMissingWhenTheReportersRan = new ArrayList<>();
    volatile List<ITestResult> foreignResultsMissingWhenTheReportersRan = new ArrayList<>();

    @Override
    public void onStart(ITestContext context) {
      suite = context.getSuite();
      ParameterSnapshots suiteSnapshots = requireNonNull(ParameterSnapshots.of(context.getSuite()));
      snapshots = suiteSnapshots;
      suiteSnapshots.requestCapture();
    }

    @Override
    public void onTestSuccess(ITestResult result) {
      results.add(result);
    }

    @Override
    public void generateReport(
        List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
      ParameterSnapshots stillHeld = ParameterSnapshots.of(suites.get(0));
      resultsMissingWhenTheReportersRan = notFoundIn(stillHeld, results);
      foreignResultsMissingWhenTheReportersRan = notFoundIn(stillHeld, foreignResults);
    }

    private static List<ITestResult> notFoundIn(
        @Nullable ParameterSnapshots snapshots, List<ITestResult> results) {
      List<ITestResult> missing = new ArrayList<>();
      for (ITestResult result : results) {
        if (snapshots == null || snapshots.find(result) == null) {
          missing.add(result);
        }
      }
      return missing;
    }
  }
}
