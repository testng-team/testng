package org.testng.reporters;

import java.util.*;
import java.io.File;

import org.testng.*;
import org.testng.internal.Utils;
import org.testng.xml.XmlSuite;

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
    for(int i = 0; i < suites.size(); i++) {
      writeSuite(xmlSuites.get(i), suites.get(i));
    }
    rootBuffer.pop();
    Utils.writeFile(outputDirectory, "testng-results.xml", rootBuffer.toString());
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
    XMLStringBuffer xmlBuffer = new XMLStringBuffer("");
    writeSuiteToBuffer(xmlBuffer, suite);
    File parentDir = suiteFile.getParentFile();
    if (parentDir.exists() || suiteFile.getParentFile().mkdirs()) {
      Utils.writeFile(parentDir.getAbsolutePath(), "testng-results.xml", xmlBuffer.toString());      
    }
  }

  private File referenceSuite(XMLStringBuffer xmlBuffer, ISuite suite) {
    String relativePath = suite.getName() + File.separatorChar + "suite.xml";
    File suiteFile = new File(config.getOutputDirectory(),  relativePath);
    Properties attrs = new Properties();
    attrs.setProperty(XMLReporterConfig.ATTR_URL, relativePath);
    xmlBuffer.addEmptyElement(XMLReporterConfig.TAG_SUITE, attrs);
    return suiteFile;
  }

  private void writeSuiteToBuffer(XMLStringBuffer xmlBuffer, ISuite suite) {
    xmlBuffer.push(XMLReporterConfig.TAG_SUITE, getSuiteAttributes(suite));
    xmlBuffer.push(XMLReporterConfig.TAG_GROUPS);
    for (String groupName : suite.getMethodsByGroups().keySet()) {
      Properties groupAttrs = new Properties();
      groupAttrs.setProperty(XMLReporterConfig.ATTR_NAME, groupName);
      xmlBuffer.push(XMLReporterConfig.TAG_GROUP, groupAttrs);
      Set<ITestNGMethod> groupMethods = getUniqueMethodSet(suite.getMethodsByGroups().get(groupName));
      for (ITestNGMethod groupMethod : groupMethods) {
        Properties methodAttrs = new Properties();
        methodAttrs.setProperty(XMLReporterConfig.ATTR_NAME, groupMethod.toString());
        xmlBuffer.addEmptyElement(XMLReporterConfig.TAG_METHOD, methodAttrs);
      }
      xmlBuffer.pop();
    }
    xmlBuffer.pop();

    Map<String, ISuiteResult> results = suite.getResults();
    XMLSuiteResultWriter suiteResultWriter = new XMLSuiteResultWriter(config);
    for (Map.Entry<String, ISuiteResult> result : results.entrySet()) {
      suiteResultWriter.writeSuiteResult(xmlBuffer, result.getValue());
    }

    xmlBuffer.pop();
  }

  private Properties getSuiteAttributes(ISuite suite) {
    Properties props = new Properties();
    props.setProperty(XMLReporterConfig.ATTR_NAME, suite.getName());
    return props;
  }


  private Set getUniqueMethodSet(Collection<ITestNGMethod> methods) {
    //TODO: Cosmin - not synchronized
    Set result = new HashSet();
    for (ITestNGMethod method : methods) {
      result.add(method);
    }
    return result;
}


}
