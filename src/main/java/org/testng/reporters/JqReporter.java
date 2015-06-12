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
  private static final String C = "class";
  private static final String D = "div";
  private static final String S = "span";

  private int m_testCount = 0;
  private String m_outputDirectory;
  private Map<String, String> m_testMap = Maps.newHashMap();

  @Override
  public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites,
      String outputDirectory) {
    m_outputDirectory = "/Users/cedric/java/misc/jquery";

    XMLStringBuffer xsb = new XMLStringBuffer("  ");
    xsb.push(D, "id", "suites");
    generateSuites(xmlSuites, suites, xsb);
    xsb.pop(D);

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
      List<ISuite> suites, XMLStringBuffer main) {
     for (ISuite suite : suites) {
      if (suite.getResults().size() == 0) {
        continue;
      }

      XMLStringBuffer xsb = new XMLStringBuffer(main.getCurrentIndent());
      XMLStringBuffer header = new XMLStringBuffer(main.getCurrentIndent());

      xsb.push(D, C, "suite-content");
      Map<String, ISuiteResult> results = suite.getResults();
      XMLStringBuffer xs1 = new XMLStringBuffer(xsb.getCurrentIndent());
      XMLStringBuffer xs2 = new XMLStringBuffer(xsb.getCurrentIndent());
      XMLStringBuffer xs3 = new XMLStringBuffer(xsb.getCurrentIndent());
      int failed = 0;
      int skipped = 0;
      int passed = 0;
      for (ISuiteResult result : results.values()) {
        ITestContext context = result.getTestContext();
        failed += context.getFailedTests().size();
        generateTests("failed", context.getFailedTests(), context, xs1);
        skipped += context.getSkippedTests().size();
        generateTests("skipped", context.getSkippedTests(), context, xs2);
        passed += context.getPassedTests().size();
        generateTests("passed", context.getPassedTests(), context, xs3);
      }
      xsb.addOptional(D, "Failed" + " tests", C, "result-banner " + "failed");
      xsb.addString(xs1.toXML());
      xsb.addOptional(D, "Skipped" + " tests", C, "result-banner " + "skipped");
      xsb.addString(xs2.toXML());
      xsb.addOptional(D, "Passed" + " tests", C, "result-banner " + "passed");
      xsb.addString(xs3.toXML());


      header.push(D, C, "suite");
      header.push(D, C, "suite-header");
      header.addOptional(S, suite.getName(), C, "suite-name");
      header.push(D, C, "stats");
      int total = failed + skipped + passed;
      String stats = String.format("%s, %d failed, %d skipped, %d passed",
          pluralize(total, "method"), failed, skipped, passed);
      header.push("ul");

      // Method stats
      header.push("li");
      header.addOptional(S, stats, C, "method-stats");
      header.pop("li");

      // Tests
      header.push("li");
      header.addOptional(S, String.format("%s ", pluralize(results.values().size(), "test"),
          C, "test-stats"));
      header.pop("li");

      // List of tests
      header.push("ul");
      for (ISuiteResult tr : results.values()) {
        String testName = tr.getTestContext().getName();
        header.push("li");
        header.addOptional("a", testName, "href", "#" + m_testMap.get(testName));
        header.pop("li");
      }
      header.pop("ul");

      header.pop("ul");
      header.pop(D);

      header.pop(D);

      main.addString(header.toXML());
      main.addString(xsb.toXML());
    }

    return main;
  }

  private String capitalize(String s) {
    return Character.toUpperCase(s.charAt(0)) + s.substring(1);
  }

  private void generateTests(String tagClass, IResultMap tests, ITestContext context,
      XMLStringBuffer xsb) {

    if (tests.getAllMethods().isEmpty()) return;

    xsb.push(D, C, "test" + (tagClass != null ? " " + tagClass : ""));
    ListMultiMap<Class<?>, ITestResult> map = Maps.newListMultiMap();
    for (ITestResult m : tests.getAllResults()) {
      map.put(m.getTestClass().getRealClass(), m);
    }

    String testName = "test-" + (m_testCount++);
    m_testMap.put(context.getName(), testName);
    xsb.push(D, C, "test-name");
    xsb.push("a", "name", testName);
    xsb.addString(context.getName());
    xsb.pop("a");

    // Expand icon
    xsb.push("a", C, "expand", "href", "#");
    xsb.addEmptyElement("img", "src", getStatusImage(tagClass));
    xsb.pop("a");

    xsb.pop(D);

    xsb.push(D, C, "test-content");
    for (Class<?> c : map.keySet()) {
      xsb.push(D, C, C);
      xsb.push(D, C, "class-header");

      // Passed/failed icon
      xsb.addEmptyElement("img", "src", getImage(tagClass));

      xsb.addOptional(S, c.getName(), C, "class-name");

      xsb.pop(D);
      xsb.push(D, C, "class-content");
      List<ITestResult> l = map.get(c);
      for (ITestResult m : l) {
        generateMethod(tagClass, m, context, xsb);
      }
      xsb.pop(D);
      xsb.pop(D);
    }
    xsb.pop(D);

    xsb.pop(D);
  }

  private static String getStatusImage(String status) {
    return "up.png";
//    if ("passed".equals(status)) return "down.png";
//    else return "up.png";
  }

  private static String getImage(String tagClass) {
    return tagClass + ".png";
  }

  private void generateMethod(String tagClass, ITestResult tr,
      ITestContext context, XMLStringBuffer xsb) {
    long time = tr.getEndMillis() - tr.getStartMillis();
    xsb.push(D, C, "method");
    xsb.push(D, C, "method-content");
    xsb.addOptional(S, tr.getMethod().getMethodName(), C, "method-name");

    // Parameters?
    if (tr.getParameters().length > 0) {
      StringBuilder sb = new StringBuilder();
      boolean first = true;
      for (Object p : tr.getParameters()) {
        if (!first) sb.append(", ");
        first = false;
        sb.append(Utils.toString(p));
      }
      xsb.addOptional(S, "(" + sb.toString() + ")", C, "parameters");
    }

    // Exception?
    if (tr.getThrowable() != null) {
      StringBuilder stackTrace = new StringBuilder();
      for (StackTraceElement str : tr.getThrowable().getStackTrace()) {
        stackTrace.append(str.toString()).append("<br>");
      }
      xsb.addOptional(D, stackTrace.toString() + "\n",
          C, "stack-trace");
    }

    xsb.addOptional(S, " " + Long.toString(time) + " ms", C, "method-time");
    xsb.pop(D);
    xsb.pop(D);
  }

  /**
   * Overridable by subclasses to create different directory names (e.g. with timestamps).
   * @param outputDirectory the output directory specified by the user
   */
  protected String generateOutputDirectoryName(String outputDirectory) {
    return outputDirectory;
  }

  private String pluralize(int count, String singular) {
    return Integer.toString(count) + " "
        + (count > 1 ? (singular.endsWith("s") ? singular + "es" : singular + "s") : singular);
  }

}
