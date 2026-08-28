package org.testng.internal.reporters;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGListener;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.reporters.snapshot.DeclaringSuiteSample;
import org.testng.reporters.snapshot.RenderingCountSample;
import org.testng.reporters.snapshot.RenderingSample;
import org.testng.reporters.snapshot.SnapshotReadingReporterSample;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;
import test.TestHelper;

/**
 * Who gets the suite's snapshots taken for them. A reporter that sits in the invocation lifecycle
 * asks for itself from {@code onStart}; one that only implements {@link IReporter} runs after every
 * invocation is over, so the run has to decide on its behalf before anything starts.
 *
 * <p>Every case is measured rather than predicted: {@link RenderingCountSample} counts the times a
 * parameter was rendered, and the {@link Inspector} reads the store back from {@code
 * generateReport}, which is the last thing that can.
 */
public class ParameterSnapshotWiringTest extends SimpleBaseTest {

  /** One sample whose parameter counts its renderings, one with the ordinary parameter shapes. */
  private static final Class<?>[] FIXTURE = {RenderingCountSample.class, RenderingSample.class};

  @Test(description = "A run no reporter reads the snapshots of renders nothing")
  public void aRunNobodyReadsRendersNothing() {
    Run run = runUnder();

    assertThat(run.renderings).isZero();
    assertThat(run.snapshottedInvocations).isEmpty();
  }

  @Test(
      description =
          "The wiring takes the snapshots of a default run, because the XML reports read them")
  public void aDefaultRunTakesSnapshots() throws IOException {
    Run run = runUnderDefaultReporters(TestHelper.createRandomDirectory());

    // XMLReporter declares the reading on AbstractXmlReporter, and a default run registers it, so
    // the question the wiring asks before any suite starts is answered yes.
    assertThat(run.snapshottedInvocations).isEqualTo(run.invocations);
    // Two: the one capture the whole run shares, and TestHTMLReporter, which is the last built-in
    // report Phase 7 has not reached and still renders the value for itself. The three that read
    // the store -- XMLReporter, jq's Main and EmailableReporter2 -- add nothing between them,
    // which is what makes this a number rather than "positive". Migrating TestHTMLReporter takes
    // it to one, and this is what will say so.
    assertThat(run.renderings).isEqualTo(2);
  }

  @Test(
      description =
          "A reporter with no invocation listener to ask from still gets the snapshots taken, and"
              + " gets them from the first invocation of the run on")
  public void aReporterThatCanOnlyReportCaptures() {
    Run run = runUnder(new SnapshotReadingReporterSample());

    assertThat(run.renderings).isEqualTo(1);
    // The whole run, from its very first invocation on -- not just the ones a listener saw late.
    assertThat(run.invocations)
        .startsWith("org.testng.reporters.snapshot.RenderingCountSample.report");
    assertThat(run.snapshottedInvocations).isEqualTo(run.invocations);
  }

  @Test(description = "A reporter that asks from the invocation lifecycle still captures")
  public void aReporterThatListensCaptures() {
    Run run = runUnder(new LifecycleReporter());

    assertThat(run.renderings).isEqualTo(1);
    assertThat(run.snapshottedInvocations).isEqualTo(run.invocations);
  }

  @Test(description = "Two reporters asking for the same snapshots render the value once")
  public void twoInterestedReportersRenderOnce() {
    Run run = runUnder(new SnapshotReadingReporterSample(), new LifecycleReporter());

    assertThat(run.renderings).isEqualTo(1);
    assertThat(run.snapshottedInvocations).isEqualTo(run.invocations);
  }

  @Test(
      description =
          "A reporter that arrives with the second suite still had the first suite's invocations"
              + " snapshotted, because the question is asked before any suite runs")
  public void aReporterDeclaredByOneSuiteCapturesTheOthers() {
    Run run =
        measure(
            create(
                createXmlSuite("reads-nothing", "counted", RenderingCountSample.class),
                createXmlSuite("declares-a-reader", "declaring", DeclaringSuiteSample.class)));

    // The first suite ran before the reporter's own suite did, and was snapshotted all the same.
    // Only it: the declaring suite's own method takes no parameter, and an invocation with nothing
    // to report is not stored -- see ParameterSnapshot#of.
    assertThat(run.renderings).isEqualTo(1);
    assertThat(run.snapshottedInvocations)
        .containsExactly("org.testng.reporters.snapshot.RenderingCountSample.report");
  }

  /**
   * The same, with the reporters a user gets when they ask for none: the run TestNG does by
   * default, whose reports are written under {@code outputDirectory}.
   */
  private static Run runUnderDefaultReporters(Path outputDirectory) {
    TestNG testng = createTests(outputDirectory, "snapshot-wiring", FIXTURE);
    testng.setUseDefaultListeners(true);
    return measure(testng);
  }

  /**
   * Runs two {@code <test>}s under the given consumers, plus the two instruments that measure what
   * happened. Neither instrument declares any interest in the snapshots, so a case that captures
   * does so because of what was passed in.
   */
  private static Run runUnder(ITestNGListener... consumers) {
    TestNG testng = createTests("snapshot-wiring", FIXTURE);
    for (ITestNGListener consumer : consumers) {
      testng.addListener(consumer);
    }
    return measure(testng);
  }

  /** Runs it under the two instruments, and reports what they saw. */
  private static Run measure(TestNG testng) {
    Observer observer = new Observer();
    Inspector inspector = new Inspector(observer);
    testng.addListener(observer);
    testng.addListener(inspector);

    int renderedBefore = RenderingCountSample.renderings();
    testng.run();

    // TestNG catches whatever a reporter throws and prints it to stderr, so an Inspector that blew
    // up would otherwise look exactly like a run that captured nothing.
    assertThat(inspector.failure).isNull();
    // And nothing is to be concluded from a run that did not run.
    assertThat(observer.results).hasSize(2);

    return new Run(
        RenderingCountSample.renderings() - renderedBefore,
        observer.invocations(),
        inspector.snapshottedInvocations);
  }

  /** What one run of {@link #runUnder} is judged on. */
  private static final class Run {

    final int renderings;

    /** The invocations that were announced, in the order they started. */
    final List<String> invocations;

    /** Those of them the store still held when the reporters ran, in the same order. */
    final List<String> snapshottedInvocations;

    Run(int renderings, List<String> invocations, List<String> snapshottedInvocations) {
      this.renderings = renderings;
      this.invocations = invocations;
      this.snapshottedInvocations = snapshottedInvocations;
    }
  }

  /** Records what was invoked, and asks for nothing: an instrument, not a consumer. */
  private static final class Observer implements ITestListener {

    final List<ITestResult> results = new ArrayList<>();

    @Override
    public void onTestStart(ITestResult result) {
      results.add(result);
    }

    List<String> invocations() {
      return results.stream().map(ParameterSnapshotWiringTest::nameOf).collect(Collectors.toList());
    }
  }

  /**
   * Reads the store back once every context has finished, which is where an {@link IReporter} would
   * read it -- and, deliberately, asks for nothing itself.
   */
  private static final class Inspector implements IReporter {

    private final Observer observer;
    final List<String> snapshottedInvocations = new ArrayList<>();

    /** Whatever went wrong in here, which TestNG would otherwise print to stderr and drop. */
    @Nullable Exception failure;

    Inspector(Observer observer) {
      this.observer = observer;
    }

    @Override
    public void generateReport(
        List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
      try {
        for (ITestResult result : observer.results) {
          // Each result's own suite, so that a run of several suites is read one store at a time.
          ITestContext context = result.getTestContext();
          ParameterSnapshots snapshots =
              context == null ? null : ParameterSnapshots.of(context.getSuite());
          if (snapshots != null && snapshots.find(result) != null) {
            snapshottedInvocations.add(nameOf(result));
          }
        }
      } catch (Exception inspecting) {
        failure = inspecting;
      }
    }
  }

  /** The route that already worked: a reporter that is told when a context starts. */
  private static final class LifecycleReporter implements ITestListener {

    @Override
    public void onStart(ITestContext context) {
      ParameterSnapshots.requestCaptureFor(context.getSuite());
    }
  }

  private static String nameOf(ITestResult result) {
    return result.getMethod().getQualifiedName();
  }
}
