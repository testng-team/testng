package org.testng.reporters;

import java.util.List;
import java.util.Properties;
import org.testng.ISuite;
import org.testng.internal.Utils;
import org.testng.xml.XmlSuite;

/** The main entry for the XML generation operation */
public class XMLReporter extends AbstractXmlReporter {

  @Override
  public void generateReport(
      List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
    if (Utils.isStringEmpty(getConfig().getOutputDirectory())) {
      getConfig().setOutputDirectory(outputDirectory);
    }

    // Calculate passed/failed/skipped
    Count count = Count.Builder.builder().build();
    for (ISuite s : suites) {
      count.add(computeCountForSuite(s));
    }

    XMLStringBuffer rootBuffer = new XMLStringBuffer();
    Properties p = writeSummaryCount(count, rootBuffer);
    rootBuffer.push(XMLReporterConfig.TAG_TESTNG_RESULTS, p);
    writeReporterOutput(rootBuffer);
    for (ISuite suite : suites) {
      writeSuite(rootBuffer, suite);
    }
    rootBuffer.pop();
    Utils.writeUtf8File(
        getConfig().getOutputDirectory(), fileName(), rootBuffer, null /* no prefix */);
  }
}
