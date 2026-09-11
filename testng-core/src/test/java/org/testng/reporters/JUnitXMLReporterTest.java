package org.testng.reporters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.annotations.Test;

/** What the JUnit XML listener does when producing its report fails. */
public class JUnitXMLReporterTest {

  @Test(description = "An Error while the report is produced costs the report, not the run")
  public void anErrorWhileProducingTheReportIsReportedAndNotRaised() {
    // This class is a listener, which TestRunner calls with nothing around it, so anything raised
    // out of generateReport ends the run. The catch there named RuntimeException, and the likeliest
    // thing to be raised while a large report is produced is an OutOfMemoryError: toXML() reads
    // the spilled file into one String and the regular expression copies it again -- the very
    // shape GITHUB-1259 and GITHUB-2334 die in. An Error is not a RuntimeException.
    //
    // The Error is raised on the one argument of the write that the context supplies, because it
    // is evaluated inside the same try as toXML() and it is the seam a test can reach.
    ITestContext context = mock(ITestContext.class);
    ISuite suite = mock(ISuite.class);
    when(suite.getName()).thenReturn("suite");
    when(context.getSuite()).thenReturn(suite);
    when(context.getName()).thenReturn("erroring");
    when(context.getExcludedMethods()).thenReturn(Collections.emptyList());
    when(context.getAllTestMethods()).thenReturn(new ITestNGMethod[0]);
    when(context.getStartInstant()).thenReturn(Instant.EPOCH);
    when(context.getEndInstant()).thenReturn(Instant.EPOCH);
    when(context.getOutputDirectory()).thenThrow(new OutOfMemoryError("Java heap space"));
    JUnitXMLReporter reporter = new JUnitXMLReporter();

    PrintStream err = System.err;
    ByteArrayOutputStream captured = new ByteArrayOutputStream();
    try (PrintStream capturing = new PrintStream(captured, true, StandardCharsets.UTF_8)) {
      System.setErr(capturing);
      assertThatCode(() -> reporter.generateReport(context)).doesNotThrowAnyException();
    } finally {
      System.setErr(err);
    }

    assertThat(captured.toString(StandardCharsets.UTF_8))
        .contains("JUnit XML report for erroring failed")
        .contains("OutOfMemoryError");
  }
}
