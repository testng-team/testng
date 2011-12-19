package org.testng.reporters;

import org.testng.IReporter;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
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

    XMLStringBuffer xsb = new XMLStringBuffer("  ");
    xsb.push("div", "id", "suites");
    generateSuites(xmlSuites, suites, xsb);
    xsb.pop("div");

    String all;
    try {
      all = Files.readFile(new File("/Users/cedric/java/misc/jquery/head"));
      Utils.writeFile(m_outputDirectory, "index2.html", all + xsb.toXML());
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private XMLStringBuffer generateSuites(List<XmlSuite> xmlSuites,
      List<ISuite> suites, XMLStringBuffer xsb) {
     for (ISuite suite : suites) {
      if (suite.getResults().size() == 0) {
        continue;
      }

      xsb.push("div", "class", "suite");
      xsb.addOptional("span", suite.getName(), "class", "suite-name");

      xsb.push("div", "class", "suite-content");
      Map<String, ISuiteResult> results = suite.getResults();
      XMLStringBuffer xs1 = new XMLStringBuffer("    ");
      XMLStringBuffer xs2 = new XMLStringBuffer("    ");
      XMLStringBuffer xs3 = new XMLStringBuffer("    ");
      for (ISuiteResult result : results.values()) {
        ITestContext context = result.getTestContext();
        generateTests("failed", context.getFailedTests(), context, xs1);
        generateTests("skipped", context.getSkippedTests(), context, xs2);
        generateTests("passed", context.getPassedTests(), context, xs3);
      }
      xsb.addOptional("div", "Failed" + " tests", "class", "result-banner " + "failed");
      xsb.addString(xs1.toXML());
      xsb.addOptional("div", "Skipped" + " tests", "class", "result-banner " + "skipped");
      xsb.addString(xs2.toXML());
      xsb.addOptional("div", "Passed" + " tests", "class", "result-banner " + "passed");
      xsb.addString(xs3.toXML());
    }
    xsb.pop("div");
    xsb.pop("div");

    return xsb;
  }

  private String capitalize(String s) {
    return Character.toUpperCase(s.charAt(0)) + s.substring(1);
  }
  private void generateTests(String tagClass, IResultMap tests, ITestContext context,
      XMLStringBuffer xsb) {

    if (tests.getAllMethods().isEmpty()) return;

    xsb.push("div", "class", "test" + (tagClass != null ? " " + tagClass : ""));
    ListMultiMap<Class<?>, ITestResult> map = Maps.newListMultiMap();
    for (ITestResult m : tests.getAllResults()) {
      map.put(m.getTestClass().getRealClass(), m);
    }

    xsb.push("a", "href", "#");
    xsb.addOptional("span", context.getName(), "class", "test-name");
    xsb.pop("a");

    xsb.push("div", "class", "test-content");
    for (Class<?> c : map.getKeys()) {
      xsb.push("div", "class", "class");
      xsb.push("div", "class", "class-header");
      xsb.addEmptyElement("img", "src", getImage(tagClass));
      xsb.addOptional("span", c.getName(), "class", "class-name");
      xsb.pop("div");
      xsb.push("div", "class", "class-content");
      List<ITestResult> l = map.get(c);
      for (ITestResult m : l) {
        generateMethod(tagClass, m, context, xsb);
      }
      xsb.pop("div");
      xsb.pop("div");
    }
    xsb.pop("div");

    xsb.pop("div");
  }

  private static String getImage(String tagClass) {
    return tagClass + ".png";
  }

  private void generateMethod(String tagClass, ITestResult tr,
      ITestContext context, XMLStringBuffer xsb) {
    long time = tr.getEndMillis() - tr.getStartMillis();
    xsb.push("div", "class", "method");
    xsb.push("div", "class", "method-content");
    xsb.addOptional("span", tr.getMethod().getMethodName(), "class", "method-name");

    // Parameters?
    if (tr.getParameters().length > 0) {
      StringBuilder sb = new StringBuilder();
      boolean first = true;
      for (Object p : tr.getParameters()) {
        if (!first) sb.append(", ");
        first = false;
        sb.append(p != null ? p.toString() : "<NULL>");
      }
      xsb.addOptional("span", "(" + sb.toString() + ")", "class", "parameters");
    }

    // Exception?
    if (tr.getThrowable() != null) {
      StringBuilder stackTrace = new StringBuilder();
      for (StackTraceElement str : tr.getThrowable().getStackTrace()) {
        stackTrace.append(str.toString()).append("<br>");
      }
      xsb.addOptional("div", stackTrace.toString() + "\n",
          "class", "stack-trace");
    }

    xsb.addOptional("span", " " + Long.toString(time) + " ms", "class", "method-time");
    xsb.pop("div");
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
