package org.testng.internal.reporters;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.testng.IConfigurationListener;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.reporters.snapshot.PassingConfigurationParameterSample;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

/**
 * How long the snapshot of a configuration method that passed is kept.
 *
 * <p>It is the one result the store is ever offered back, because it is the one a reporter sitting
 * in the invocation lifecycle is finished with the moment it succeeds. Whether that offer is taken
 * depends on the other kind of reader: {@code testng-results.xml} lists the configurations that
 * passed, and it is written once every invocation of the run is over.
 *
 * <p>Both cases are measured the same way -- a probe collects the configurations as they succeed
 * and reads the store back from {@link IReporter#generateReport}, which is the last moment one is
 * readable -- so what differs between them is only which request was made.
 */
public class PassedConfigurationSnapshotTest extends SimpleBaseTest {

  @Test(
      description =
          "A reporter that reads once the invocations are over still finds the snapshot of a"
              + " configuration that passed")
  public void aLateReaderStillFindsThePassedConfiguration() {
    LateProbe probe = new LateProbe();

    run(probe);

    assertThat(probe.failure).isNull();
    assertThat(probe.renderedWhenTheReportersRan).containsExactly("[before-configuration]");
  }

  @Test(
      description =
          "A run whose only readers are live ones still drops it, so the store does not grow for a"
              + " run that gains nothing from it")
  public void aRunWithNoLateReaderStillDiscards() {
    LifecycleProbe probe = new LifecycleProbe();

    run(probe);

    assertThat(probe.failure).isNull();
    // Announced, captured, printed, and gone: the null is the discard, not a capture that never
    // happened -- the case above proves the same invocation is snapshotted when someone reads late.
    assertThat(probe.renderedWhenTheReportersRan).containsExactly((String) null);
  }

  @Test(
      description =
          "A store still holding a passed configuration when the reporters are done is released"
              + " all the same -- the first case where detachFrom has anything to drop")
  public void aStoreThatKeptAPassedConfigurationIsStillReleased() {
    LateProbe probe = new LateProbe();

    run(probe);

    assertThat(probe.failure).isNull();
    assertThat(probe.suite).isNotNull();
    assertThat(ParameterSnapshots.of(probe.suite)).isNull();
    assertThat(probe.snapshots).isNotNull();
    assertThat(probe.snapshots.isEmpty()).isTrue();
  }

  private static void run(Probe probe) {
    TestNG testng = create(PassingConfigurationParameterSample.class);
    testng.addListener(probe);
    testng.run();
  }

  /**
   * Collects the configurations that passed while they are announced, and reads back what the store
   * still held for them once every context had finished.
   */
  private abstract static class Probe implements IConfigurationListener, IReporter {

    private final List<ITestResult> passed = Collections.synchronizedList(new ArrayList<>());

    final List<@Nullable String> renderedWhenTheReportersRan = new ArrayList<>();

    volatile @Nullable ISuite suite;
    volatile @Nullable ParameterSnapshots snapshots;

    /** Whatever went wrong in here, which TestNG would otherwise print to stderr and drop. */
    volatile @Nullable Exception failure;

    @Override
    public void onConfigurationSuccess(ITestResult result) {
      passed.add(result);
    }

    @Override
    public void generateReport(
        List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
      try {
        ISuite reported = suites.get(0);
        suite = reported;
        ParameterSnapshots stillHeld = ParameterSnapshots.of(reported);
        snapshots = stillHeld;
        for (ITestResult result : passed) {
          ParameterSnapshot snapshot = stillHeld == null ? null : stillHeld.find(result);
          renderedWhenTheReportersRan.add(
              snapshot == null ? null : String.join(", ", snapshot.renderedValues()));
        }
      } catch (Exception inspecting) {
        failure = inspecting;
      }
    }
  }

  /** The route the XML reports take: no invocation lifecycle to ask from, so the run asks. */
  private static final class LateProbe extends Probe implements ParameterSnapshotReader {}

  /** The route {@code TextReporter} and {@code VerboseReporter} take. */
  private static final class LifecycleProbe extends Probe implements ITestListener {

    @Override
    public void onStart(ITestContext context) {
      ParameterSnapshots.requestCaptureFor(context.getSuite());
    }
  }
}
