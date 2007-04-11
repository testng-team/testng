package org.testng.reporters;

import org.testng.*;
import org.testng.internal.Utils;
import org.testng.xml.XmlSuite;

import java.io.File;
import java.util.*;

/**
 * The main entry for the XML generation operation
 *
 * @author Cosmin Marginean, Mar 16, 2007
 */
public class XMLReporter implements IReporter {

  private XMLReporterConfig config;
  private XMLStringBuffer rootBuffer;

  public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
    config = new XMLReporterConfig(outputDirectory);

    rootBuffer = new XMLStringBuffer("");
    rootBuffer.push(XMLReporterConfig.TAG_TESTNG_RESULTS);
    writeReporterOutput(rootBuffer);
    for (int i = 0; i < suites.size(); i++) {
      writeSuite(xmlSuites.get(i), suites.get(i));
    }
    rootBuffer.pop();
    Utils.writeFile(config.getOutput(), "testng-results.xml", rootBuffer.toXML());
  }

  private void writeReporterOutput(XMLStringBuffer xmlBuffer) {
    //TODO: Cosmin - maybe a <line> element isn't indicated for each line. Also escaping might be considered
    xmlBuffer.push(XMLReporterConfig.TAG_REPORTER_OUTPUT);
    List<String> output = Reporter.getOutput();    
    for (String line : output) {
      xmlBuffer.addRequired(XMLReporterConfig.TAG_LINE, line);
    }
    xmlBuffer.pop();
  }

  private void writeSuite(XmlSuite xmlSuite, ISuite suite) {
    Utils.writeFile(config.getOutput(),  xmlSuite.getName() + "-testng.xml", xmlSuite.toXml());
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
    XMLStringBuffer xmlBuffer = new XMLStringBuffer("");
    writeSuiteToBuffer(xmlBuffer, suite);
    File parentDir = suiteFile.getParentFile();
    if (parentDir.exists() || suiteFile.getParentFile().mkdirs()) {
      Utils.writeFile(parentDir.getAbsolutePath(), "testng-results.xml", xmlBuffer.toXML());
    }
  }

  private File referenceSuite(XMLStringBuffer xmlBuffer, ISuite suite) {
    String relativePath = suite.getName() + File.separatorChar + "testng-results.xml";
    File suiteFile = new File(config.getOutput(), relativePath);
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
    for (String groupName : methodsByGroups.keySet()) {
      Properties groupAttrs = new Properties();
      groupAttrs.setProperty(XMLReporterConfig.ATTR_NAME, groupName);
      xmlBuffer.push(XMLReporterConfig.TAG_GROUP, groupAttrs);
      Set<ITestNGMethod> groupMethods = getUniqueMethodSet(methodsByGroups.get(groupName));
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
    return props;
  }


  private Set<ITestNGMethod> getUniqueMethodSet(Collection<ITestNGMethod> methods) {
    Set<ITestNGMethod> result = new LinkedHashSet<ITestNGMethod>();
    for (ITestNGMethod method : methods) {
      result.add(method);
    }
    return result;
  }
}
