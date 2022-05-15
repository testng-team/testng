package org.testng.reporters;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import org.testng.IDataProviderMethod;
import org.testng.IResultMap;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.CustomAttribute;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.collections.Sets;
import org.testng.internal.Utils;
import org.testng.util.Strings;
import org.testng.util.TimeUtils;

/**
 * Utility writing an ISuiteResult to an XMLStringBuffer. Depending on the settings in the <code>
 * config</code> property it might generate an additional XML file with the actual content and only
 * reference the file with an <code>url</code> attribute in the passed XMLStringBuffer.
 *
 * @author Cosmin Marginean, Mar 16, 2007
 */
public class XMLSuiteResultWriter {

  private final XMLReporterConfig config;
  private final ICustomizeXmlReport customizer;

  public XMLSuiteResultWriter(XMLReporterConfig config, ICustomizeXmlReport customizer) {
    this.config = config;
    this.customizer = customizer;
  }

  /**
   * Writes the specified ISuiteResult in the given XMLStringBuffer. Please consider that depending
   * on the settings in the <code>config</code> property it might generate an additional XML file
   * with the actual content and only reference the file with an <code>url</code> attribute in the
   * passed XMLStringBuffer.
   *
   * @param xmlBuffer The XML buffer where to write or reference the suite result
   * @param suiteResult The <code>ISuiteResult</code> to serialize
   */
  public void writeSuiteResult(XMLStringBuffer xmlBuffer, ISuiteResult suiteResult) {
    if (XMLReporterConfig.FF_LEVEL_SUITE_RESULT != config.getFileFragmentationLevel()) {
      writeAllToBuffer(xmlBuffer, suiteResult);
    } else {
      String parentDir =
          config.getOutputDirectory()
              + File.separatorChar
              + suiteResult.getTestContext().getSuite().getName();
      File file = referenceSuiteResult(xmlBuffer, parentDir, suiteResult);
      XMLStringBuffer suiteXmlBuffer = new XMLStringBuffer();
      writeAllToBuffer(suiteXmlBuffer, suiteResult);
      Utils.writeUtf8File(
          file.getAbsoluteFile().getParent(), file.getName(), suiteXmlBuffer.toXML());
    }
  }

  private void writeAllToBuffer(XMLStringBuffer xmlBuffer, ISuiteResult suiteResult) {
    xmlBuffer.push(XMLReporterConfig.TAG_TEST, getSuiteResultAttributes(suiteResult));
    Set<ITestResult> testResults = Sets.newLinkedHashSet();
    ITestContext testContext = suiteResult.getTestContext();
    addAllTestResults(testResults, testContext.getPassedTests());
    addAllTestResults(testResults, testContext.getFailedTests());
    addAllTestResults(testResults, testContext.getSkippedTests());
    addAllTestResults(testResults, testContext.getPassedConfigurations());
    addAllTestResults(testResults, testContext.getSkippedConfigurations());
    addAllTestResults(testResults, testContext.getFailedConfigurations());
    addAllTestResults(testResults, testContext.getFailedButWithinSuccessPercentageTests());
    addTestResults(xmlBuffer, testResults);
    xmlBuffer.pop();
  }

  private void addAllTestResults(Set<ITestResult> testResults, IResultMap resultMap) {
    if (resultMap != null) {
      // Sort the results chronologically before adding them
      testResults.addAll(
          resultMap.getAllResults().stream()
              .sorted((o1, o2) -> (int) (o1.getStartMillis() - o2.getStartMillis()))
              .collect(Collectors.toCollection(LinkedHashSet::new)));
    }
  }

  private File referenceSuiteResult(
      XMLStringBuffer xmlBuffer, String parentDir, ISuiteResult suiteResult) {
    Properties attrs = new Properties();
    String suiteResultName = suiteResult.getTestContext().getName() + ".xml";
    attrs.setProperty(XMLReporterConfig.ATTR_URL, suiteResultName);
    xmlBuffer.addEmptyElement(XMLReporterConfig.TAG_TEST, attrs);
    return new File(parentDir + File.separatorChar + suiteResultName);
  }

  private Properties getSuiteResultAttributes(ISuiteResult suiteResult) {
    Properties attributes = new Properties();
    ITestContext tc = suiteResult.getTestContext();
    attributes.setProperty(XMLReporterConfig.ATTR_NAME, tc.getName());
    XMLReporter.addDurationAttributes(config, attributes, tc.getStartDate(), tc.getEndDate());
    return attributes;
  }

  private void addTestResults(XMLStringBuffer xmlBuffer, Set<ITestResult> testResults) {
    Map<String, List<ITestResult>> testsGroupedByClass = buildTestClassGroups(testResults);
    for (Map.Entry<String, List<ITestResult>> result : testsGroupedByClass.entrySet()) {
      Properties attributes = new Properties();
      String className = result.getKey();
      if (config.isSplitClassAndPackageNames()) {
        int dot = className.lastIndexOf('.');
        attributes.setProperty(
            XMLReporterConfig.ATTR_NAME, dot > -1 ? className.substring(dot + 1) : className);
        attributes.setProperty(
            XMLReporterConfig.ATTR_PACKAGE, dot > -1 ? className.substring(0, dot) : "[default]");
      } else {
        attributes.setProperty(XMLReporterConfig.ATTR_NAME, className);
      }

      xmlBuffer.push(XMLReporterConfig.TAG_CLASS, attributes);
      List<ITestResult> sortedResults = result.getValue();
      Collections.sort(sortedResults);
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
      List<ITestResult> list = map.computeIfAbsent(className, k -> Lists.newArrayList());
      list.add(result);
    }
    return map;
  }

  private void addTestResult(XMLStringBuffer xmlBuffer, ITestResult testResult) {
    Properties attribs = getTestResultAttributes(testResult);
    attribs.setProperty(XMLReporterConfig.ATTR_STATUS, getStatusString(testResult.getStatus()));
    if (testResult.wasRetried()) {
      attribs.setProperty(XMLReporterConfig.ATTR_RETRIED, Boolean.TRUE.toString());
    }
    xmlBuffer.push(XMLReporterConfig.TAG_TEST_METHOD, attribs);
    addTestMethodParams(xmlBuffer, testResult);
    addTestResultException(xmlBuffer, testResult);
    addTestResultOutput(xmlBuffer, testResult);
    if (config.isGenerateTestResultAttributes()) {
      addTestResultAttributes(xmlBuffer, testResult);
    }
    addTestMethodCustomAttributes(xmlBuffer, testResult);
    customizer.addCustomTagsFor(xmlBuffer, testResult);
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
      default:
        throw new AssertionError("Unexpected value: " + testResultStatus);
    }
  }

  private Properties getTestResultAttributes(ITestResult testResult) {
    Properties attributes = new Properties();
    if (!testResult.getMethod().isTest()) {
      attributes.setProperty(XMLReporterConfig.ATTR_IS_CONFIG, "true");
    }
    attributes.setProperty(XMLReporterConfig.ATTR_NAME, testResult.getMethod().getMethodName());
    String testInstanceName = testResult.getTestName();
    if (null != testInstanceName) {
      attributes.setProperty(XMLReporterConfig.ATTR_TEST_INSTANCE_NAME, testInstanceName);
    }
    String description = testResult.getMethod().getDescription();
    if (!Utils.isStringEmpty(description)) {
      attributes.setProperty(XMLReporterConfig.ATTR_DESC, description);
    }

    attributes.setProperty(
        XMLReporterConfig.ATTR_METHOD_SIG, removeClassName(testResult.getMethod().toString()));

    String startTime =
        TimeUtils.formatTimeInLocalOrSpecifiedTimeZone(
            testResult.getStartMillis(), config.getTimestampFormat());
    String endTime =
        TimeUtils.formatTimeInLocalOrSpecifiedTimeZone(
            testResult.getEndMillis(), config.getTimestampFormat());
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

    IDataProviderMethod dp = testResult.getMethod().getDataProviderMethod();
    if (dp != null) {
      String dataProvider = dp.getName();
      if (!Strings.isNullOrEmpty(dataProvider)) {
        attributes.setProperty(XMLReporterConfig.ATTR_DATA_PROVIDER, dataProvider);
      }
    }

    return attributes;
  }

  private String removeClassName(String methodSignature) {
    int firstParanthesisPos = methodSignature.indexOf("(");
    int dotAferClassPos = methodSignature.substring(0, firstParanthesisPos).lastIndexOf(".");
    return methodSignature.substring(dotAferClassPos + 1);
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

      XMLReporterConfig.StackTraceLevels level = calculateStackTraceLevels(testResult);
      switch (level) {
        case SHORT:
          xmlBuffer.push(XMLReporterConfig.TAG_SHORT_STACKTRACE);
          xmlBuffer.addCDATA(Utils.shortStackTrace(exception, false));
          xmlBuffer.pop();
          break;
        case FULL:
          xmlBuffer.push(XMLReporterConfig.TAG_FULL_STACKTRACE);
          xmlBuffer.addCDATA(Utils.longStackTrace(exception, false));
          xmlBuffer.pop();
          break;
        default:
          // everything else is ignored for now.
      }

      xmlBuffer.pop();
    }
  }

  private XMLReporterConfig.StackTraceLevels calculateStackTraceLevels(ITestResult testResult) {
    XMLReporterConfig.StackTraceLevels stackTraceoutputMethod = config.getStackTraceOutput();
    if (testResult.isSuccess()) {
      stackTraceoutputMethod = config.getStackTraceOutputLevelForPassedTests();
    }
    return stackTraceoutputMethod;
  }

  private void addTestResultOutput(XMLStringBuffer xmlBuffer, ITestResult testResult) {
    // TODO: Cosmin - maybe a <line> element isn't indicated for each line
    xmlBuffer.push(XMLReporterConfig.TAG_REPORTER_OUTPUT);
    List<String> output = Reporter.getOutput(testResult);
    for (String line : output) {
      if (line != null) {
        xmlBuffer.push(XMLReporterConfig.TAG_LINE);
        xmlBuffer.addCDATA(line);
        xmlBuffer.pop();
      }
    }
    xmlBuffer.pop();
  }

  private void addTestResultAttributes(XMLStringBuffer xmlBuffer, ITestResult testResult) {
    if (testResult.getAttributeNames() != null && testResult.getAttributeNames().size() > 0) {
      xmlBuffer.push(XMLReporterConfig.TAG_ATTRIBUTES);
      for (String attrName : testResult.getAttributeNames()) {
        if (attrName == null) {
          continue;
        }
        Object attrValue = testResult.getAttribute(attrName);

        Properties attributeAttrs = new Properties();
        attributeAttrs.setProperty(XMLReporterConfig.ATTR_NAME, attrName);
        if (attrValue == null) {
          attributeAttrs.setProperty(XMLReporterConfig.ATTR_IS_NULL, "true");
          xmlBuffer.addEmptyElement(XMLReporterConfig.TAG_ATTRIBUTE, attributeAttrs);
        } else {
          xmlBuffer.push(XMLReporterConfig.TAG_ATTRIBUTE, attributeAttrs);
          xmlBuffer.addCDATA(attrValue.toString());
          xmlBuffer.pop();
        }
      }
      xmlBuffer.pop();
    }
  }

  private void addTestMethodCustomAttributes(XMLStringBuffer xmlBuffer, ITestResult testResult) {
    CustomAttribute[] attributes = testResult.getMethod().getAttributes();
    if (attributes == null || attributes.length == 0) {
      return;
    }
    xmlBuffer.push(XMLReporterConfig.TAG_CUSTOM_ATTRIBUTES);
    for (CustomAttribute attribute : attributes) {
      xmlBuffer.push(XMLReporterConfig.TAG_CUSTOM_ATTRIBUTE);
      Properties properties = new Properties();
      properties.setProperty(XMLReporterConfig.TAG_CUSTOM_ATTRIBUTE_VALUE, attribute.name());
      xmlBuffer.push(XMLReporterConfig.TAG_CUSTOM_ATTRIBUTE_NAME, properties);
      xmlBuffer.pop();
      xmlBuffer.push(XMLReporterConfig.TAG_CUSTOM_ATTRIBUTE_VALUE);
      xmlBuffer.addCDATA(Arrays.toString(attribute.values()));
      xmlBuffer.pop();
      xmlBuffer.pop();
    }
    xmlBuffer.pop();
  }
}
