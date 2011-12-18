package org.testng.reporters;

import org.testng.IReporter;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.collections.ListMultiMap;
import org.testng.collections.Maps;
import org.testng.internal.Utils;
import org.testng.xml.XmlSuite;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JqReporter implements IReporter {

  private String m_outputDirectory;

  @Override
  public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites,
      String outputDirectory) {
    m_outputDirectory = "/Users/cedric/java/misc/jquery";

    XMLStringBuffer xsb = new XMLStringBuffer();
    xsb.push("div", "id", "suites");
    createReport(xmlSuites, suites, xsb);
    xsb.pop("div");

    String all;
    try {
      all = Files.readFile(new File("/Users/cedric/java/misc/jquery/head"));
      Utils.writeFile(m_outputDirectory, "index-all.html", all + xsb.toXML());
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private XMLStringBuffer createReport(List<XmlSuite> xmlSuites,
      List<ISuite> suites, XMLStringBuffer xsb) {
     for (ISuite suite : suites) {
      if (suite.getResults().size() == 0) {
        continue;
      }

      xsb.push("h2");
      xsb.addOptional("span", suite.getName(), "class", "suite-name");
      xsb.pop("h2");

      Map<String, ISuiteResult> results = suite.getResults();
      XMLStringBuffer xs1 = new XMLStringBuffer();
      XMLStringBuffer xs2 = new XMLStringBuffer();
      XMLStringBuffer xs3 = new XMLStringBuffer();
      for (ISuiteResult result : results.values()) {
        ITestContext context = result.getTestContext();
        generateTests("failure", context.getFailedTests(), context, xs1);
        generateTests("skip", context.getSkippedTests(), context, xs2);
        generateTests("success", context.getPassedTests(), context, xs3);
      }
      xsb.addString(xs1.toXML());
      xsb.addString(xs2.toXML());
      xsb.addString(xs3.toXML());
    }

    return xsb;
  }

  private void generateTests(String tagClass, IResultMap tests, ITestContext context,
      XMLStringBuffer xsb) {

    if (tests.getAllMethods().isEmpty()) return;

    xsb.push("div", "class", "test" + (tagClass != null ? " " + tagClass : ""));
    ListMultiMap<Class<?>, ITestResult> map = Maps.newListMultiMap();
    for (ITestNGMethod m : tests.getAllMethods()) {
      for (ITestResult result : tests.getResults(m)) {
        map.put(m.getTestClass().getRealClass(), result);
      }
    }

    xsb.push("h3");
    xsb.push("a", "href", "#");
    xsb.addOptional("span", context.getName(), "class", "test-name");
    xsb.pop("a");
    xsb.pop("h3");

    xsb.push("div", "class", "test-content");
    for (Class<?> c : map.getKeys()) {
      xsb.addOptional("span", c.getName(), "class", "class-name");
      List<ITestResult> l = map.get(c);
      for (ITestResult tr : l) {
        generateMethod(tagClass, tr, context, xsb);
      }
    }
    xsb.pop("div");

    xsb.pop("div");
  }

  private void generateMethod(String tagClass, ITestResult tr,
      ITestContext context, XMLStringBuffer xsb) {
    long time = tr.getEndMillis() - tr.getStartMillis();
    xsb.push("div", "class", "method");
    xsb.addOptional("span", tr.getMethod().getMethodName(), "class", "method-name");
    xsb.addOptional("span", Long.toString(time), "class", "method-time");
    xsb.pop("div");
  }

  /**
   * Overridable by subclasses to create different directory names (e.g. with timestamps).
   * @param outputDirectory the output directory specified by the user
   */
  protected String generateOutputDirectoryName(String outputDirectory) {
    return outputDirectory;
  }
}
