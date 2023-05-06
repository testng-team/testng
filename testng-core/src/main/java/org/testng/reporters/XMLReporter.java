package org.testng.reporters;

import java.util.Date;
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

  /**
   * @deprecated - This method stands deprecated as of TestNG <code>7.8.0</code> Add started-at,
   *     finished-at and duration-ms attributes to the <code>&lt;suite&gt;</code> tag
   * @param config The reporter config
   * @param attributes The properties
   * @param minStartDate The minimum start date
   * @param maxEndDate The maximum end date
   */
  @Deprecated
  public static void addDurationAttributes(
      XMLReporterConfig config, Properties attributes, Date minStartDate, Date maxEndDate) {
    setDurationAttributes(config, attributes, minStartDate, maxEndDate);
  }
}
