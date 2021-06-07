package org.testng.reporters;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.internal.Utils;
import org.testng.util.TimeUtils;
import org.testng.xml.XmlSuite;

/** The main entry for the XML generation operation */
public class XMLReporter implements IReporter, ICustomizeXmlReport {

  private final XMLReporterConfig config = new XMLReporterConfig();
  private XMLStringBuffer rootBuffer;

  @Override
  public void generateReport(
      List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
    if (Utils.isStringEmpty(config.getOutputDirectory())) {
      config.setOutputDirectory(outputDirectory);
    }

    // Calculate passed/failed/skipped
    int passed = 0;
    int failed = 0;
    int skipped = 0;
    int ignored = 0;
    int retried = 0;
    for (ISuite s : suites) {
      Map<String, ISuiteResult> suiteResults = s.getResults();
      for (ISuiteResult sr : suiteResults.values()) {
        ITestContext testContext = sr.getTestContext();
        passed += testContext.getPassedTests().size();
        failed += testContext.getFailedTests().size();
        int retriedPerTest = 0;
        int skippedPerTest = 0;
        for (ITestResult result : testContext.getSkippedTests().getAllResults()) {
          if (result.wasRetried()) {
            retriedPerTest++;
          } else {
            skippedPerTest++;
          }
        }
        skipped += skippedPerTest;
        retried += retriedPerTest;
        ignored += testContext.getExcludedMethods().size();
      }
    }

    rootBuffer = new XMLStringBuffer();
    Properties p = new Properties();
    p.put("passed", passed);
    p.put("failed", failed);
    p.put("skipped", skipped);
    if (retried > 0) {
      p.put("retried", retried);
    }
    p.put("ignored", ignored);
    p.put("total", passed + failed + skipped + ignored + retried);
    rootBuffer.push(XMLReporterConfig.TAG_TESTNG_RESULTS, p);
    writeReporterOutput(rootBuffer);
    for (ISuite suite : suites) {
      writeSuite(suite);
    }
    rootBuffer.pop();
    Utils.writeUtf8File(config.getOutputDirectory(), fileName(), rootBuffer, null /* no prefix */);
  }

  @Override
  public void addCustomTagsFor(XMLStringBuffer xmlBuffer, ITestResult testResult) {}

  public String fileName() {
    return RuntimeBehavior.getDefaultFileNameForXmlReports();
  }

  private void writeReporterOutput(XMLStringBuffer xmlBuffer) {
    // TODO: Cosmin - maybe a <line> element isn't indicated for each line
    xmlBuffer.push(XMLReporterConfig.TAG_REPORTER_OUTPUT);
    List<String> output = Reporter.getOutput();
    for (String line : output) {
      if (line != null) {
        xmlBuffer.push(XMLReporterConfig.TAG_LINE);
        xmlBuffer.addCDATA(line);
        xmlBuffer.pop();
      }
    }
    xmlBuffer.pop();
  }

  private void writeSuite(ISuite suite) {
    switch (config.getFileFragmentationLevel()) {
      case XMLReporterConfig.FF_LEVEL_NONE:
        writeSuiteToBuffer(rootBuffer, suite);
        break;
      case XMLReporterConfig.FF_LEVEL_SUITE:
      case XMLReporterConfig.FF_LEVEL_SUITE_RESULT:
        File suiteFile = referenceSuite(rootBuffer, suite);
        writeSuiteToFile(suiteFile, suite);
        break;
      default:
        throw new AssertionError("Unexpected value: " + config.getFileFragmentationLevel());
    }
  }

  private void writeSuiteToFile(File suiteFile, ISuite suite) {
    XMLStringBuffer xmlBuffer = new XMLStringBuffer();
    writeSuiteToBuffer(xmlBuffer, suite);
    File parentDir = suiteFile.getParentFile();
    suiteFile.getParentFile().mkdirs();
    if (parentDir.exists() || suiteFile.getParentFile().exists()) {
      Utils.writeUtf8File(parentDir.getAbsolutePath(), fileName(), xmlBuffer.toXML());
    }
  }

  private File referenceSuite(XMLStringBuffer xmlBuffer, ISuite suite) {
    String relativePath = suite.getName() + File.separatorChar + fileName();
    File suiteFile = new File(config.getOutputDirectory(), relativePath);
    Properties attrs = new Properties();
    attrs.setProperty(XMLReporterConfig.ATTR_URL, relativePath);
    xmlBuffer.addEmptyElement(XMLReporterConfig.TAG_SUITE, attrs);
    return suiteFile;
  }

  private void writeSuiteToBuffer(XMLStringBuffer xmlBuffer, ISuite suite) {
    xmlBuffer.push(XMLReporterConfig.TAG_SUITE, getSuiteAttributes(suite));
    writeSuiteGroups(xmlBuffer, suite);

    Map<String, ISuiteResult> results = suite.getResults();
    XMLSuiteResultWriter suiteResultWriter = new XMLSuiteResultWriter(config, this);
    for (Map.Entry<String, ISuiteResult> result : results.entrySet()) {
      suiteResultWriter.writeSuiteResult(xmlBuffer, result.getValue());
    }

    xmlBuffer.pop();
  }

  private void writeSuiteGroups(XMLStringBuffer xmlBuffer, ISuite suite) {
    xmlBuffer.push(XMLReporterConfig.TAG_GROUPS);
    Map<String, Collection<ITestNGMethod>> methodsByGroups = suite.getMethodsByGroups();
    for (Map.Entry<String, Collection<ITestNGMethod>> entry : methodsByGroups.entrySet()) {
      Properties groupAttrs = new Properties();
      groupAttrs.setProperty(XMLReporterConfig.ATTR_NAME, entry.getKey());
      xmlBuffer.push(XMLReporterConfig.TAG_GROUP, groupAttrs);
      Set<ITestNGMethod> groupMethods = getUniqueMethodSet(entry.getValue());
      for (ITestNGMethod groupMethod : groupMethods) {
        Properties methodAttrs = new Properties();
        methodAttrs.setProperty(XMLReporterConfig.ATTR_NAME, groupMethod.getMethodName());
        methodAttrs.setProperty(XMLReporterConfig.ATTR_METHOD_SIG, groupMethod.toString());
        methodAttrs.setProperty(XMLReporterConfig.ATTR_CLASS, groupMethod.getRealClass().getName());
        xmlBuffer.addEmptyElement(XMLReporterConfig.TAG_METHOD, methodAttrs);
      }
      xmlBuffer.pop();
    }
    xmlBuffer.pop();
  }

  private Properties getSuiteAttributes(ISuite suite) {
    Properties props = new Properties();
    props.setProperty(XMLReporterConfig.ATTR_NAME, suite.getName());

    // Calculate the duration
    Map<String, ISuiteResult> results = suite.getResults();
    Date minStartDate = new Date();
    Date maxEndDate = null;
    // TODO: We could probably optimize this in order not to traverse this twice
    for (Map.Entry<String, ISuiteResult> result : results.entrySet()) {
      ITestContext testContext = result.getValue().getTestContext();
      Date startDate = testContext.getStartDate();
      Date endDate = testContext.getEndDate();
      if (minStartDate.after(startDate)) {
        minStartDate = startDate;
      }
      if (maxEndDate == null || maxEndDate.before(endDate)) {
        maxEndDate = endDate != null ? endDate : startDate;
      }
    }
    // The suite could be completely empty
    if (maxEndDate == null) {
      maxEndDate = minStartDate;
    }
    addDurationAttributes(config, props, minStartDate, maxEndDate);
    return props;
  }

  /**
   * Add started-at, finished-at and duration-ms attributes to the <code>&lt;suite&gt;</code> tag
   *
   * @param config The reporter config
   * @param attributes The properties
   * @param minStartDate The minimum start date
   * @param maxEndDate The maximum end date
   */
  public static void addDurationAttributes(
      XMLReporterConfig config, Properties attributes, Date minStartDate, Date maxEndDate) {

    String startTime =
        TimeUtils.formatTimeInLocalOrSpecifiedTimeZone(
            minStartDate.getTime(), config.getTimestampFormat());
    String endTime =
        TimeUtils.formatTimeInLocalOrSpecifiedTimeZone(
            maxEndDate.getTime(), config.getTimestampFormat());
    long duration = maxEndDate.getTime() - minStartDate.getTime();

    attributes.setProperty(XMLReporterConfig.ATTR_STARTED_AT, startTime);
    attributes.setProperty(XMLReporterConfig.ATTR_FINISHED_AT, endTime);
    attributes.setProperty(XMLReporterConfig.ATTR_DURATION_MS, Long.toString(duration));
  }

  private Set<ITestNGMethod> getUniqueMethodSet(Collection<ITestNGMethod> methods) {
    return new LinkedHashSet<>(methods);
  }

  @Override
  public XMLReporterConfig getConfig() {
    return config;
  }
}
