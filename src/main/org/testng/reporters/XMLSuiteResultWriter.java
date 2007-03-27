package org.testng.reporters;

import org.testng.IResultMap;
import org.testng.ISuiteResult;
import org.testng.ITestResult;
import org.testng.internal.Utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Properties;

/**
 * Utility writing an ISuiteResult to an XMLStringBuffer. Depending on the settings in the <code>config</code> property
 * it might generate an additional XML file with the actual content and only reference the file with an <code>url</code>
 * attribute in the passed XMLStringBuffer.
 *
 * @author Cosmin Marginean, Mar 16, 2007
 */

public class XMLSuiteResultWriter {

  private XMLReporterConfig config;

  public XMLSuiteResultWriter(XMLReporterConfig config) {
    this.config = config;
  }

  /**
   * Writes the specified ISuiteResult in the given XMLStringBuffer. Please consider that depending on the settings in
   * the <code>config</code> property it might generate an additional XML file with the actual content and only
   * reference the file with an <code>url</code> attribute in the passed XMLStringBuffer.
   *
   * @param xmlBuffer   The XML buffer where to write or reference the suite result
   * @param suiteResult The <code>ISuiteResult</code> to serialize
   */
  public void writeSuiteResult(XMLStringBuffer xmlBuffer, ISuiteResult suiteResult) {
    if (XMLReporterConfig.FF_LEVEL_SUITE_RESULT != config.getFileFragmentationLevel()) {
      writeAllToBuffer(xmlBuffer, suiteResult);
    } else {
      String parentDir =
              config.getOutputDirectory() + File.separatorChar + suiteResult.getTestContext().getSuite().getName();
      File file = referenceSuiteResult(xmlBuffer, parentDir, suiteResult);
      XMLStringBuffer suiteXmlBuffer = new XMLStringBuffer("");
      suiteXmlBuffer.push(XMLReporterConfig.TAG_TESTNG_RESULTS);
      suiteXmlBuffer.push(XMLReporterConfig.TAG_SUITE,
              XMLReporter.getSuiteAttributes(suiteResult.getTestContext().getSuite()));
      writeAllToBuffer(suiteXmlBuffer, suiteResult);
      suiteXmlBuffer.pop();
      suiteXmlBuffer.pop();
      Utils.writeFile(file.getAbsoluteFile().getParent(), file.getName(), suiteXmlBuffer.toXML());
    }
  }

  private void writeAllToBuffer(XMLStringBuffer xmlBuffer, ISuiteResult suiteResult) {
    xmlBuffer.push(XMLReporterConfig.TAG_TEST, getSuiteResultAttributes(suiteResult));
    addTestResults(xmlBuffer, XMLReporterConfig.TAG_PASSED, suiteResult.getTestContext().getPassedTests());
    addTestResults(xmlBuffer, XMLReporterConfig.TAG_FAILED, suiteResult.getTestContext().getFailedTests());
    addTestResults(xmlBuffer, XMLReporterConfig.TAG_SKIPPED, suiteResult.getTestContext().getSkippedTests());
    xmlBuffer.pop();
  }

  private File referenceSuiteResult(XMLStringBuffer xmlBuffer, String parentDir, ISuiteResult suiteResult) {
    Properties attrs = new Properties();
    String suiteResultName = suiteResult.getTestContext().getName() + ".xml";
    attrs.setProperty(XMLReporterConfig.ATTR_URL, suiteResultName);
    xmlBuffer.addEmptyElement(XMLReporterConfig.TAG_TEST, attrs);
    return new File(parentDir + File.separatorChar + suiteResultName);
  }

  private Properties getSuiteResultAttributes(ISuiteResult suiteResult) {
    Properties attributes = new Properties();
    attributes.setProperty(XMLReporterConfig.ATTR_NAME, suiteResult.getTestContext().getName());
    return attributes;
  }

  private void addTestResults(XMLStringBuffer xmlBuffer, String tagName, IResultMap results) {
    xmlBuffer.push(tagName);
    for (ITestResult testResult : results.getAllResults()) {
      addTestResult(xmlBuffer, testResult);
    }
    xmlBuffer.pop();
  }

  private void addTestResult(XMLStringBuffer xmlBuffer, ITestResult testResult) {
    xmlBuffer.push(XMLReporterConfig.TAG_TEST_METHOD, getTestResultAttributes(testResult));
    addTestMethodParams(xmlBuffer, testResult);
    addTestResultException(xmlBuffer, testResult);
    xmlBuffer.pop();
  }

  private Properties getTestResultAttributes(ITestResult testResult) {
    Properties attributes = new Properties();
    attributes.setProperty(XMLReporterConfig.ATTR_NAME, testResult.getName());
    attributes.setProperty(XMLReporterConfig.ATTR_METHOD, testResult.getMethod().toString());

    //TODO: Cosmin - not finished, constants
    SimpleDateFormat format = new SimpleDateFormat(XMLReporterConfig.FMT_DAY_MONTH_YEAR_TIME);
    String startTime = format.format(testResult.getStartMillis());
    String endTime = format.format(testResult.getEndMillis());
    attributes.setProperty("started-at", startTime);
    attributes.setProperty("finished-at", endTime);
    long duration = testResult.getEndMillis() - testResult.getStartMillis();
    String strDuration = Long.toString(duration);
    attributes.setProperty("duration-ms", strDuration);


    if (config.isGenerateGroupsAttribute()) {
      String groupNamesStr = getGroupNamesString(testResult);
      if (!Utils.isStringEmpty(groupNamesStr)) {
        attributes.setProperty(XMLReporterConfig.ATTR_GROUPS, groupNamesStr);
      }
    }
    return attributes;
  }

  public void addTestMethodParams(XMLStringBuffer xmlBuffer, ITestResult testResult) {
    Object[] parameters = testResult.getParameters();
    if ((parameters != null) && (parameters.length > 0)) {
      xmlBuffer.push(XMLReporterConfig.TAG_PARAMS);
      for (int i = 0; i < parameters.length; i++) {
        addParameter(xmlBuffer, parameters[i], i);
      }
      xmlBuffer.pop();
    }
  }

  private void addParameter(XMLStringBuffer xmlBuffer, Object parameter, int i) {
    Properties attrs = new Properties();
    attrs.setProperty(XMLReporterConfig.ATTR_INDEX, String.valueOf(i));
    xmlBuffer.push(XMLReporterConfig.TAG_PARAM, attrs);
    if (parameter == null) {
      Properties valueAttrs = new Properties();
      valueAttrs.setProperty(XMLReporterConfig.ATTR_IS_NULL, "true");
      xmlBuffer.addEmptyElement(XMLReporterConfig.TAG_PARAM_VALUE, valueAttrs);
    } else {
      xmlBuffer.push(XMLReporterConfig.TAG_PARAM_VALUE);
      xmlBuffer.addCDATA(parameter.toString());
      xmlBuffer.pop();
    }
    xmlBuffer.pop();
  }

  private void addTestResultException(XMLStringBuffer xmlBuffer, ITestResult testResult) {
    Throwable exception = testResult.getThrowable();
    if (exception != null) {
      Properties exceptionAttrs = new Properties();
      exceptionAttrs.setProperty(XMLReporterConfig.ATTR_CLASS, exception.getClass().getName());
      xmlBuffer.push(XMLReporterConfig.TAG_EXCEPTION, exceptionAttrs);

      if (!Utils.isStringEmpty(exception.getMessage())) {
        xmlBuffer.push(XMLReporterConfig.TAG_MESSAGE);
        xmlBuffer.addCDATA(exception.getMessage());
        xmlBuffer.pop();
      }

      String[] stackTraces = Utils.stackTrace(exception, true);
      if ((config.getStackTraceOutputMethod() & XMLReporterConfig.STACKTRACE_SHORT) == XMLReporterConfig
              .STACKTRACE_SHORT) {
        xmlBuffer.push(XMLReporterConfig.TAG_SHORT_STACKTRACE);
        xmlBuffer.addCDATA(stackTraces[0]);
        xmlBuffer.pop();
      }
      if ((config.getStackTraceOutputMethod() & XMLReporterConfig.STACKTRACE_FULL) == XMLReporterConfig.STACKTRACE_FULL)
      {
        xmlBuffer.push(XMLReporterConfig.TAG_FULL_STACKTRACE);
        xmlBuffer.addCDATA(stackTraces[1]);
        xmlBuffer.pop();
      }

      xmlBuffer.pop();
    }
  }

  private String getGroupNamesString(ITestResult testResult) {
    String result = "";
    String[] groupNames = testResult.getMethod().getGroups();
    if ((groupNames != null) && (groupNames.length > 0)) {
      for (int i = 0; i < groupNames.length; i++) {
        result += groupNames[i];
        if (i < groupNames.length - 1) {
          result += ", ";
        }
      }
    }
    return result;
  }
}