package org.testng.reporters;

import org.testng.IResultMap;
import org.testng.ISuiteResult;
import org.testng.ITestResult;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.internal.Utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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
      writeAllToBuffer(suiteXmlBuffer, suiteResult);
      Utils.writeUtf8File(file.getAbsoluteFile().getParent(), file.getName(), suiteXmlBuffer.toXML());
    }
  }

  private void writeAllToBuffer(XMLStringBuffer xmlBuffer, ISuiteResult suiteResult) {
    xmlBuffer.push(XMLReporterConfig.TAG_TEST, getSuiteResultAttributes(suiteResult));
    Set<ITestResult> testResults = new HashSet();
    addAllTestResults(testResults, suiteResult.getTestContext().getPassedTests());
    addAllTestResults(testResults, suiteResult.getTestContext().getFailedTests());
    addAllTestResults(testResults, suiteResult.getTestContext().getSkippedTests());
    addAllTestResults(testResults, suiteResult.getTestContext().getPassedConfigurations());
    addAllTestResults(testResults, suiteResult.getTestContext().getSkippedConfigurations());
    addAllTestResults(testResults, suiteResult.getTestContext().getFailedConfigurations());
    addAllTestResults(testResults, suiteResult.getTestContext().getFailedButWithinSuccessPercentageTests());
    addTestResults(xmlBuffer, testResults);
    xmlBuffer.pop();
  }

  private void addAllTestResults(Set<ITestResult> testResults, IResultMap resultMap) {
    if (resultMap != null) {
      testResults.addAll(resultMap.getAllResults());
    }
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

  private void addTestResults(XMLStringBuffer xmlBuffer, Set<ITestResult> testResults) {
    Map<String, List<ITestResult>> testsGroupedByClass = buildTestClassGroups(testResults);
    for (Map.Entry<String, List<ITestResult>> result : testsGroupedByClass.entrySet()) {
      Properties attributes = new Properties();
      String className = result.getKey();
      if (config.isSplitClassAndPackageNames()) {
        int dot = className.lastIndexOf('.');
        attributes.setProperty(XMLReporterConfig.ATTR_NAME,
                dot > -1 ? className.substring(dot + 1, className.length()) : className);
        attributes.setProperty(XMLReporterConfig.ATTR_PACKAGE, dot > -1 ? className.substring(0, dot) : "[default]");
      } else {
        attributes.setProperty(XMLReporterConfig.ATTR_NAME, className);
      }

      xmlBuffer.push(XMLReporterConfig.TAG_CLASS, attributes);
      List<ITestResult> sortedResults = result.getValue();
      Collections.sort( sortedResults );
      for (ITestResult testResult : sortedResults) {
        addTestResult(xmlBuffer, testResult);
      }
      xmlBuffer.pop();
    }
  }

  private Map<String, List<ITestResult>> buildTestClassGroups(Set<ITestResult> testResults) {
    Map<String, List<ITestResult>> map = Maps.newHashMap();
    for (ITestResult result : testResults) {
      String className = result.getTestClass().getName();
      List<ITestResult> list = map.get(className);
      if (list == null) {
        list = Lists.newArrayList();
        map.put(className, list);
      }
      list.add(result);
    }
    return map;
  }

  private void addTestResult(XMLStringBuffer xmlBuffer, ITestResult testResult) {
    Properties attribs = getTestResultAttributes(testResult);
    attribs.setProperty(XMLReporterConfig.ATTR_STATUS, getStatusString(testResult.getStatus()));
    xmlBuffer.push(XMLReporterConfig.TAG_TEST_METHOD, attribs);
    addTestMethodParams(xmlBuffer, testResult);
    addTestResultException(xmlBuffer, testResult);
    xmlBuffer.pop();
  }

  private String getStatusString(int testResultStatus) {
    switch (testResultStatus) {
      case ITestResult.SUCCESS:
        return "PASS";
      case ITestResult.FAILURE:
        return "FAIL";
      case ITestResult.SKIP:
        return "SKIP";
      case ITestResult.SUCCESS_PERCENTAGE_FAILURE:
        return "SUCCESS_PERCENTAGE_FAILURE";
    }
    return null;
  }

  private Properties getTestResultAttributes(ITestResult testResult) {
    Properties attributes = new Properties();
    if (!testResult.getMethod().isTest()) {
      attributes.setProperty(XMLReporterConfig.ATTR_IS_CONFIG, "true");
    }
    attributes.setProperty(XMLReporterConfig.ATTR_NAME, testResult.getName());
    String description = testResult.getMethod().getDescription();
    if (!Utils.isStringEmpty(description)) {
      attributes.setProperty(XMLReporterConfig.ATTR_DESC, description);
    }

    attributes.setProperty(XMLReporterConfig.ATTR_METHOD_SIG, removeClassName(testResult.getMethod().toString()));

    SimpleDateFormat format = new SimpleDateFormat(config.getTimestampFormat());
    String startTime = format.format(testResult.getStartMillis());
    String endTime = format.format(testResult.getEndMillis());
    attributes.setProperty(XMLReporterConfig.ATTR_STARTED_AT, startTime);
    attributes.setProperty(XMLReporterConfig.ATTR_FINISHED_AT, endTime);
    long duration = testResult.getEndMillis() - testResult.getStartMillis();
    String strDuration = Long.toString(duration);
    attributes.setProperty(XMLReporterConfig.ATTR_DURATION_MS, strDuration);

    if (config.isGenerateGroupsAttribute()) {
      String groupNamesStr = Utils.arrayToString(testResult.getMethod().getGroups());
      if (!Utils.isStringEmpty(groupNamesStr)) {
        attributes.setProperty(XMLReporterConfig.ATTR_GROUPS, groupNamesStr);
      }
    }

    if (config.isGenerateDependsOnMethods()) {
      String dependsOnStr = Utils.arrayToString(testResult.getMethod().getMethodsDependedUpon());
      if (!Utils.isStringEmpty(dependsOnStr)) {
        attributes.setProperty(XMLReporterConfig.ATTR_DEPENDS_ON_METHODS, dependsOnStr);
      }
    }

    if (config.isGenerateDependsOnGroups()) {
      String dependsOnStr = Utils.arrayToString(testResult.getMethod().getGroupsDependedUpon());
      if (!Utils.isStringEmpty(dependsOnStr)) {
        attributes.setProperty(XMLReporterConfig.ATTR_DEPENDS_ON_GROUPS, dependsOnStr);
      }
    }

    return attributes;
  }

  private String removeClassName(String methodSignature) {
    int firstParanthesisPos = methodSignature.indexOf("(");
    int dotAferClassPos = methodSignature.substring(0, firstParanthesisPos).lastIndexOf(".");
    return methodSignature.substring(dotAferClassPos + 1, methodSignature.length());
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

      String[] stackTraces = Utils.stackTrace(exception, false);
      if ((config.getStackTraceOutputMethod() & XMLReporterConfig.STACKTRACE_SHORT) == XMLReporterConfig
              .STACKTRACE_SHORT) {
        xmlBuffer.push(XMLReporterConfig.TAG_SHORT_STACKTRACE);
        xmlBuffer.addCDATA(stackTraces[0]);
        xmlBuffer.pop();
      }
      if ((config.getStackTraceOutputMethod() & XMLReporterConfig.STACKTRACE_FULL) == XMLReporterConfig
              .STACKTRACE_FULL) {
        xmlBuffer.push(XMLReporterConfig.TAG_FULL_STACKTRACE);
        xmlBuffer.addCDATA(stackTraces[1]);
        xmlBuffer.pop();
      }

      xmlBuffer.pop();
    }
  }

}
