package org.testng.internal.reporters;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.testng.IConfigurationListener;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.reporters.snapshot.ConfigurationParameterSample;
import org.testng.reporters.snapshot.SkippedConfigurationSample;
import org.testng.reporters.snapshot.SkippedDataDrivenSample;
import org.testng.reporters.snapshot.SkippedInvocationCountSample;
import org.testng.reporters.snapshot.SkippedParallelInvocationCountSample;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

/**
 * What an invocation carries at the moment TestNG announces it is starting.
 *
 * <p>The invariant every path here is held to: an invocation is announced with the values it will
 * be reported with, and nothing assigns them afterwards. A listener reading {@code onTestStart} or
 * {@code beforeConfiguration} would otherwise see a result that is not yet what it will be reported
 * as -- and {@link ParameterSnapshotRecorder}, which is one, would have nothing to capture. The
 * last test here follows that consequence through to what the store holds once the run is over.
 *
 * <p>Asserted twice over: that the values are the ones expected rather than empty, and that the
 * array they are held in is still the one the announcement saw. {@code setParameters} always
 * installs a fresh array, so its identity is exactly "nothing has re-assigned these since".
 */
public class ParameterAnnouncementTest extends SimpleBaseTest {

  @Test(
      description =
          "A data-driven test skipped by a dependency is announced with the row it would have run")
  public void dataDrivenSkipsAreAnnouncedWithTheirValues() {
    Probe probe = new Probe();
    TestNG testng = create(SkippedDataDrivenSample.class);
    testng.setReportAllDataDrivenTestsAsSkipped(true);

    run(testng, probe);

    assertNothingWasAssignedAfterAnnouncing(probe);
    assertThat(probe.announcedValuesOf("skipped")).containsExactly("first", "second");
  }

  @Test(description = "A cancelled invocationCount is announced with the row it would have re-run")
  public void cancelledInvocationsAreAnnouncedWithTheirValues() {
    Probe probe = new Probe();

    run(create(SkippedInvocationCountSample.class), probe);

    assertNothingWasAssignedAfterAnnouncing(probe);
    // The invocation that failed, then the two that never got to run.
    assertThat(probe.announcedValuesOf("cancelled"))
        .containsExactly("only-row", "only-row", "only-row");
  }

  @Test(
      description =
          "A cancelled invocationCount of a parallel data provider is announced with the row it"
              + " would have re-run, like the sequential one")
  public void cancelledParallelInvocationsAreAnnouncedWithTheirValues() {
    Probe probe = new Probe();

    run(create(SkippedParallelInvocationCountSample.class), probe);

    assertNothingWasAssignedAfterAnnouncing(probe);
    // The invocation that failed, then the two that never got to run -- the same three the
    // sequential shape announces, carrying the same row.
    assertThat(probe.announcedValuesOf("cancelled"))
        .containsExactly("only-row", "only-row", "only-row");
  }

  @Test(
      description =
          "A test skipped because its configuration failed is announced with the values that"
              + " configuration was handed")
  public void testsSkippedForAConfigurationFailureAreAnnouncedWithTheirValues() {
    Probe probe = new Probe();

    run(create(ConfigurationParameterSample.class), probe);

    assertNothingWasAssignedAfterAnnouncing(probe);
    assertThat(probe.announcedValuesOf("report")).containsExactly("mutated");
  }

  @Test(
      description =
          "A configuration skipped because an earlier one failed is announced with nothing, and"
              + " reports nothing")
  public void skippedConfigurationsAreAnnouncedWithNothing() {
    Probe probe = new Probe();

    run(create(SkippedConfigurationSample.class), probe);

    // Its parameters are never computed: resolving them for a method that will not run can throw,
    // and that throw would turn this skip into a failure. Announced with nothing, reported as
    // nothing -- consistently, which is what the invariant asks of it.
    assertNothingWasAssignedAfterAnnouncing(probe);
    assertThat(probe.announcedValuesOf("skipped")).isEmpty();
  }

  @Test(
      description =
          "The values a data-driven skip is reported with come from the snapshot, not from reading"
              + " the result back at report time")
  public void dataDrivenSkipsAreReportedThroughTheSnapshot() {
    Probe probe = new Probe();
    TestNG testng = create(SkippedDataDrivenSample.class);
    testng.setReportAllDataDrivenTestsAsSkipped(true);

    run(testng, probe);

    assertThat(probe.snapshotsWhenTheReportersRanOf("skipped"))
        .containsExactly("\"first\"", "\"second\"");
  }

  private static void run(TestNG testng, Probe probe) {
    testng.addListener(probe);
    testng.run();
  }

  private static void assertNothingWasAssignedAfterAnnouncing(Probe probe) {
    assertThat(probe.announcements).isNotEmpty().allMatch(Announcement::stillHoldsWhatItAnnounced);
  }

  /**
   * Records what every invocation carries when it is announced. It also stands in for a reporter,
   * declaring its interest when a context starts and reading the store back from {@link
   * IReporter#generateReport}, which is the last moment one is readable.
   *
   * <p>One of the samples has a parallel data provider, so announcements reach this from a
   * data-provider worker thread as well as from the runner one.
   */
  private static final class Probe implements ITestListener, IConfigurationListener, IReporter {

    final List<Announcement> announcements = Collections.synchronizedList(new ArrayList<>());
    private final Map<Announcement, String> snapshotsWhenTheReportersRan = new HashMap<>();

    @Override
    public void onStart(ITestContext context) {
      ParameterSnapshots.requestCaptureFor(context.getSuite());
    }

    @Override
    public void onTestStart(ITestResult result) {
      announcements.add(new Announcement(result));
    }

    @Override
    public void beforeConfiguration(ITestResult result) {
      announcements.add(new Announcement(result));
    }

    @Override
    public void generateReport(
        List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
      ParameterSnapshots snapshots = requireNonNull(ParameterSnapshots.of(suites.get(0)));
      for (Announcement announcement : announcements) {
        snapshotsWhenTheReportersRan.put(announcement, announcement.renderedBy(snapshots));
      }
    }

    /** Every value announced for the named method, flattened, in announcement order. */
    List<String> announcedValuesOf(String methodName) {
      List<String> values = new ArrayList<>();
      for (Announcement announcement : announcements) {
        if (announcement.isOf(methodName)) {
          for (Object value : announcement.whenAnnounced) {
            values.add(String.valueOf(value));
          }
        }
      }
      return values;
    }

    /** What the store held for the named method's results, once every context had finished. */
    List<String> snapshotsWhenTheReportersRanOf(String methodName) {
      List<String> rendered = new ArrayList<>();
      for (Announcement announcement : announcements) {
        if (announcement.isOf(methodName)) {
          rendered.add(snapshotsWhenTheReportersRan.get(announcement));
        }
      }
      return rendered;
    }
  }

  /** One invocation, and the values it was announced with. */
  private static final class Announcement {

    private final ITestResult result;
    private final Object[] whenAnnounced;

    Announcement(ITestResult result) {
      this.result = result;
      this.whenAnnounced = result.getParameters();
    }

    boolean isOf(String methodName) {
      return result.getMethod().getMethodName().equals(methodName);
    }

    /**
     * Read live, so this covers everything up to the assertion rather than only up to the callback
     * that ended the invocation.
     */
    boolean stillHoldsWhatItAnnounced() {
      return result.getParameters() == whenAnnounced;
    }

    @Nullable
    String renderedBy(ParameterSnapshots snapshots) {
      ParameterSnapshot snapshot = snapshots.find(result);
      return snapshot == null ? null : String.join(", ", snapshot.renderedValues());
    }

    @Override
    public String toString() {
      return result.getMethod().getMethodName()
          + " announced with "
          + Arrays.toString(whenAnnounced)
          + ", now holding "
          + Arrays.toString(result.getParameters());
    }
  }
}
