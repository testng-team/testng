package org.testng.reporters.snapshot;

import java.util.List;
import org.testng.ISuite;
import org.testng.internal.reporters.ParameterSnapshotReader;
import org.testng.xml.XmlSuite;

/**
 * A reporter that declares it reads the parameter snapshots and has no invocation lifecycle to
 * declare it from -- the shape this wiring exists for.
 *
 * <p>Top level, and public, so that a sample class can name it in {@code @Listeners}: a reporter
 * that arrives with one suite rather than with the run is the case the decision has to be taken
 * before <em>any</em> suite starts for.
 */
public class SnapshotReadingReporterSample implements ParameterSnapshotReader {

  @Override
  public void generateReport(
      List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {}
}
