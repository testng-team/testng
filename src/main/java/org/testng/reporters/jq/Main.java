package org.testng.reporters.jq;

import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ITestResult;
import org.testng.internal.Utils;
import org.testng.reporters.Files;
import org.testng.reporters.XMLStringBuffer;
import org.testng.xml.XmlSuite;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class Main implements IReporter {
  public static final String C = "class";
  public static final String D = "div";
  public static final String S = "span";

  private static final String PASSED = "passed";
  private static final String SKIPPED = "skipped";
  private static final String FAILED = "failed";

  private static final String[] RESOURCES = new String[] {
    "jquery-1.7.1.min.js", "testng-reports.css", "testng-reports.js",
    "passed.png", "failed.png", "skipped.png", "navigator-bullet.png"
  };

  private Model m_model;
  private String m_outputDirectory;

  @Override
  public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites,
      String outputDirectory) {
    m_model = new Model(suites);
    m_outputDirectory = outputDirectory;

    XMLStringBuffer xsb = new XMLStringBuffer("  ");
    xsb.push(D, C, "navigator-root");
    new NavigatorPanel(m_model).generate(suites, xsb);
    xsb.pop(D);

    xsb.push(D, C, "wrapper");
    xsb.push(D, "class", "main-panel-root");

    //
    // Suite panels
    //
    for (ISuite s : suites) {
      generateSuitePanel(s, xsb, m_model.getTag(s));
    }

    //
    // Panel that displays the list of test names
    //
    new TestPanel().generate(suites, xsb);

    //
    // Panel that displays the content of testng.xml
    //
    new TestNgXmlPanel().generate(suites, xsb);

    xsb.pop(D); // main-panel-root
    xsb.pop(D); // wrapper

    String all;
    try {
      InputStream head3 = getClass().getResourceAsStream("/head3");
      if (head3 == null) {
        throw new RuntimeException("Couldn't find resource head3");
      } else {
        for (String fileName : RESOURCES) {
          Files.copyFile(getClass().getResourceAsStream("/" + fileName),
              new File(m_outputDirectory, fileName));
        }
        all = Files.readFile(head3);
        Utils.writeFile(m_outputDirectory, "index3.html", all + xsb.toXML());
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private void generateSuitePanel(ISuite suite, XMLStringBuffer xsb, String divName) {
    xsb.push(D, C, "panel " + divName);
    {
      ResultsByClass byClass = m_model.getFailedResultsByClass(suite);
      for (Class<?> c : byClass.getClasses()) {
        generateClassPanel(c, byClass.getResults(c), xsb, FAILED);
      }
      byClass = m_model.getSkippedResultsByClass(suite);
      for (Class<?> c : byClass.getClasses()) {
        generateClassPanel(c, byClass.getResults(c), xsb, SKIPPED);
      }
      byClass = m_model.getPassedResultsByClass(suite);
      for (Class<?> c : byClass.getClasses()) {
        generateClassPanel(c, byClass.getResults(c), xsb, PASSED);
      }
    }
    xsb.pop(D);
  }

  private void generateClassPanel(Class c, List<ITestResult> results,XMLStringBuffer xsb,
      String status) {
    xsb.push(D, C, "class-header rounded-window-top");

    // Passed/failed icon
    xsb.addEmptyElement("img", "src", getImage(status));
    xsb.addOptional(S, c.getName(), C, "class-name");
    xsb.pop(D);

    xsb.push(D, C, "class-content rounded-window-bottom");

    for (ITestResult tr : results) {
      generateMethod(tr, xsb);
    }
    xsb.pop(D);
  }

  private void generateMethod(ITestResult tr, XMLStringBuffer xsb) {
    xsb.push(D, C, "method");
    xsb.push(D, C, "method-content");
    xsb.addEmptyElement("a", "name", Model.getTestResultName(tr));
    xsb.addOptional(S, tr.getMethod().getMethodName(), C, "method-name");

    // Parameters?
    if (tr.getParameters().length > 0) {
      StringBuilder sb = new StringBuilder();
      boolean first = true;
      for (Object p : tr.getParameters()) {
        if (!first) sb.append(", ");
        first = false;
        sb.append(p != null ? p.toString() : "<NULL>");
      }
      xsb.addOptional(S, "(" + sb.toString() + ")", C, "parameters");
    }

    // Exception?
    if (tr.getStatus() != ITestResult.SUCCESS && tr.getThrowable() != null) {
      StringBuilder stackTrace = new StringBuilder();
      stackTrace.append("<b>\"")
              .append(tr.getThrowable().getMessage())
              .append("\"</b>")
              .append("<br>");
      for (StackTraceElement str : tr.getThrowable().getStackTrace()) {
        stackTrace.append(str.toString()).append("<br>");
      }
      xsb.addOptional(D, stackTrace.toString() + "\n",
          C, "stack-trace");
    }

//    long time = tr.getEndMillis() - tr.getStartMillis();
//    xsb.addOptional(S, " " + Long.toString(time) + " ms", C, "method-time");
    xsb.pop(D);
    xsb.pop(D);
  }

  public static String getImage(String tagClass) {
    return tagClass + ".png";
  }
}
