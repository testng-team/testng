package org.testng.reporters;

import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.Reporter;
import org.testng.internal.Utils;
import org.testng.xml.XmlSuite;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;

/**
 * The main entry for the XML generation operation
 * 
 * @author Cosmin Marginean, Mar 16, 2007
 */
public class XMLReporter implements IReporter {
  public static final String FILE_NAME = "testng-results.xml";

  private final XMLReporterConfig config = new XMLReporterConfig();
  private XMLStringBuffer rootBuffer;

  @Override
  public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites,
      String outputDirectory) {
    if (Utils.isStringEmpty(config.getOutputDirectory())) {
      config.setOutputDirectory(outputDirectory);
    }

    // Calculate passed/failed/skipped
    int passed = 0;
    int failed = 0;
    int skipped = 0;
    for (ISuite s : suites) {
      for (ISuiteResult sr : s.getResults().values()) {
        ITestContext testContext = sr.getTestContext();
        passed += testContext.getPassedTests().size();
        failed += testContext.getFailedTests().size();
        skipped += testContext.getSkippedTests().size();
      }
    }

    rootBuffer = new XMLStringBuffer();
    Properties p = new Properties();
    p.put("passed", passed);
    p.put("failed", failed);
    p.put("skipped", skipped);
    p.put("total", passed + failed + skipped);
    rootBuffer.push(XMLReporterConfig.TAG_TESTNG_RESULTS, p);
    writeReporterOutput(rootBuffer);
    for (ISuite suite : suites) {
      writeSuite(suite.getXmlSuite(), suite);
    }
    rootBuffer.pop();
    Utils.writeUtf8File(config.getOutputDirectory(), FILE_NAME, rootBuffer, null /* no prefix */);
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

  private void writeSuite(XmlSuite xmlSuite, ISuite suite) {
    switch (config.getFileFragmentationLevel()) {
    case XMLReporterConfig.FF_LEVEL_NONE:
      writeSuiteToBuffer(rootBuffer, suite);
      break;
    case XMLReporterConfig.FF_LEVEL_SUITE:
    case XMLReporterConfig.FF_LEVEL_SUITE_RESULT:
      File suiteFile = referenceSuite(rootBuffer, suite);
      writeSuiteToFile(suiteFile, suite);
    }
  }

  private void writeSuiteToFile(File suiteFile, ISuite suite) {
    XMLStringBuffer xmlBuffer = new XMLStringBuffer();
    writeSuiteToBuffer(xmlBuffer, suite);
    File parentDir = suiteFile.getParentFile();
    if (parentDir.exists() || suiteFile.getParentFile().mkdirs()) {
      Utils.writeFile(parentDir.getAbsolutePath(), FILE_NAME, xmlBuffer.toXML());
    }
  }

  private File referenceSuite(XMLStringBuffer xmlBuffer, ISuite suite) {
    String relativePath = suite.getName() + File.separatorChar + FILE_NAME;
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
    XMLSuiteResultWriter suiteResultWriter = new XMLSuiteResultWriter(config);
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
   * Add started-at, finished-at and duration-ms attributes to the <suite> tag
   */
  public static void addDurationAttributes(XMLReporterConfig config, Properties attributes,
      Date minStartDate, Date maxEndDate) {
    SimpleDateFormat format = new SimpleDateFormat(config.getTimestampFormat());
    TimeZone utc = TimeZone.getTimeZone("UTC");
    format.setTimeZone(utc);
    String startTime = format.format(minStartDate);
    String endTime = format.format(maxEndDate);
    long duration = maxEndDate.getTime() - minStartDate.getTime();

    attributes.setProperty(XMLReporterConfig.ATTR_STARTED_AT, startTime);
    attributes.setProperty(XMLReporterConfig.ATTR_FINISHED_AT, endTime);
    attributes.setProperty(XMLReporterConfig.ATTR_DURATION_MS, Long.toString(duration));
  }

  private Set<ITestNGMethod> getUniqueMethodSet(Collection<ITestNGMethod> methods) {
    Set<ITestNGMethod> result = new LinkedHashSet<>();
    for (ITestNGMethod method : methods) {
      result.add(method);
    }
    return result;
  }

  // TODO: This is not the smartest way to implement the config
  public int getFileFragmentationLevel() {
    return config.getFileFragmentationLevel();
  }

  public void setFileFragmentationLevel(int fileFragmentationLevel) {
    config.setFileFragmentationLevel(fileFragmentationLevel);
  }

  public int getStackTraceOutputMethod() {
    return config.getStackTraceOutputMethod();
  }

  public void setStackTraceOutputMethod(int stackTraceOutputMethod) {
    config.setStackTraceOutputMethod(stackTraceOutputMethod);
  }

  public String getOutputDirectory() {
    return config.getOutputDirectory();
  }

  public void setOutputDirectory(String outputDirectory) {
    config.setOutputDirectory(outputDirectory);
  }

  public boolean isGenerateGroupsAttribute() {
    return config.isGenerateGroupsAttribute();
  }

  public void setGenerateGroupsAttribute(boolean generateGroupsAttribute) {
    config.setGenerateGroupsAttribute(generateGroupsAttribute);
  }

  public boolean isSplitClassAndPackageNames() {
    return config.isSplitClassAndPackageNames();
  }

  public void setSplitClassAndPackageNames(boolean splitClassAndPackageNames) {
    config.setSplitClassAndPackageNames(splitClassAndPackageNames);
  }

  public String getTimestampFormat() {
    return config.getTimestampFormat();
  }

  public void setTimestampFormat(String timestampFormat) {
    config.setTimestampFormat(timestampFormat);
  }

  public boolean isGenerateDependsOnMethods() {
    return config.isGenerateDependsOnMethods();
  }

  public void setGenerateDependsOnMethods(boolean generateDependsOnMethods) {
    config.setGenerateDependsOnMethods(generateDependsOnMethods);
  }

  public void setGenerateDependsOnGroups(boolean generateDependsOnGroups) {
    config.setGenerateDependsOnGroups(generateDependsOnGroups);
  }

  public boolean isGenerateDependsOnGroups() {
    return config.isGenerateDependsOnGroups();
  }

  public void setGenerateTestResultAttributes(boolean generateTestResultAttributes) {
    config.setGenerateTestResultAttributes(generateTestResultAttributes);
  }

  public boolean isGenerateTestResultAttributes() {
    return config.isGenerateTestResultAttributes();
  }

}
