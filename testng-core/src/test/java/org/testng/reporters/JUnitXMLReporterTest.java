package org.testng.reporters;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Collections;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.annotations.Test;

/** What the JUnit XML listener does when producing its report fails. */
public class JUnitXMLReporterTest {

  @Test(description = "An Error while the report is produced costs the report, not the run")
  public void anErrorWhileProducingTheReportIsReportedAndNotRaised() {
    // This class is a listener, which TestRunner calls with nothing around it, so anything raised
    // out of generateReport ends the run -- and the catch there named RuntimeException, which an
    // OutOfMemoryError from toXML() is not.
    //
    // The Error is raised on the one argument of the write that the context supplies, because it
    // is evaluated inside the same try as toXML() and it is the seam a test can reach. It is the
    // first argument, so nothing after it -- the file name, the document -- is ever asked for.
    ITestContext context = mock(ITestContext.class);
    when(context.getName()).thenReturn("erroring");
    when(context.getExcludedMethods()).thenReturn(Collections.emptyList());
    when(context.getAllTestMethods()).thenReturn(new ITestNGMethod[0]);
    when(context.getStartInstant()).thenReturn(Instant.EPOCH);
    when(context.getEndInstant()).thenReturn(Instant.EPOCH);
    when(context.getOutputDirectory()).thenThrow(new OutOfMemoryError("Java heap space"));
    JUnitXMLReporter reporter = new JUnitXMLReporter();

    // The report is logged as lost, not asserted on here: the test logger caches its stream at
    // start-up, so nothing in-process can read it back. BrokenSpillTest reads the message from a
    // child JVM's output, which is what covers that half.
    assertThatCode(() -> reporter.generateReport(context)).doesNotThrowAnyException();
  }
}
