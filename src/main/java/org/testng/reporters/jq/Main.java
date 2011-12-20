package org.testng.reporters.jq;

import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestResult;
import org.testng.collections.ListMultiMap;
import org.testng.collections.Maps;
import org.testng.internal.Utils;
import org.testng.reporters.Files;
import org.testng.reporters.XMLStringBuffer;
import org.testng.xml.XmlSuite;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main implements IReporter {
  private static final String C = "class";
  private static final String D = "div";
  private static final String S = "span";

  private ListMultiMap<ISuite, ITestResult> model = Maps.newListMultiMap();
  private String m_outputDirectory;

  @Override
  public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites,
      String outputDirectory) {
    initModel(suites);
    m_outputDirectory = "/Users/cedric/java/misc/jquery";

    XMLStringBuffer xsb = new XMLStringBuffer("  ");
    xsb.push(D, "class", "main-panel-root");

    int suiteCount = 0;
    for (ISuite s : suites) {
      generateSuitePanel(s, xsb, "suite-" + suiteCount++);
//      System.out.println("Suite:" + s.getName());
//      for (ITestResult r : model.get(s)) {
//        System.out.println("  TestResult: " + r);
//      }
    }
    generateTestPanel(xsb, "test-panel");
    xsb.pop(D);

    String all;
    try {
      all = Files.readFile(new File("/Users/cedric/java/misc/jquery/head3"));
      Utils.writeFile(m_outputDirectory, "index3.html", all + xsb.toXML());
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private void generateTestPanel(XMLStringBuffer xsb, String divName) {
    xsb.push(D, C, "panel " + divName);
    xsb.addString("TEST PANEL");
    xsb.pop(D);
  }

  private void generateSuitePanel(ISuite suite, XMLStringBuffer xsb, String divName) {
    xsb.push(D, C, "panel " + divName);
    xsb.addOptional(S, suite.getName(), C, "suite-name");
    for (ITestResult tr : model.get(suite)) {
      xsb.push(D, C, "suite-content");
      xsb.addOptional(S, tr.getName() + " " + tr.getStatus(), C, "test-name");
      xsb.pop(D);
    }
    xsb.pop(D);
  }

  private void initModel(List<ISuite> suites) {
    for (ISuite suite : suites) {
      for (ISuiteResult sr : suite.getResults().values()) {
        model.putAll(suite, sr.getTestContext().getFailedTests().getAllResults());
      }
      for (ISuiteResult sr : suite.getResults().values()) {
        model.putAll(suite, sr.getTestContext().getSkippedTests().getAllResults());
      }
      for (ISuiteResult sr : suite.getResults().values()) {
        model.putAll(suite, sr.getTestContext().getPassedTests().getAllResults());
      }
    }
  }
}
