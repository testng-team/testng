package org.testng.reporters;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.TestRunner;
import org.testng.internal.Utils;
import org.testng.log4testng.Logger;

/** This class implements an HTML reporter for individual tests. */
public class TestHTMLReporter implements ITestListener {
  private static final Comparator<ITestResult> NAME_COMPARATOR = new NameComparator();
  private static final Comparator<ITestResult> CONFIGURATION_COMPARATOR =
      new ConfigurationComparator();

  private ITestContext m_testContext = null;

  /////
  // implements ITestListener
  //
  @Override
  public void onStart(ITestContext context) {
    m_testContext = context;
  }

  @Override
  public void onFinish(ITestContext context) {
    generateLog(
        m_testContext,
        null /* host */,
        m_testContext.getOutputDirectory(),
        context.getFailedConfigurations().getAllResults(),
        context.getSkippedConfigurations().getAllResults(),
        context.getPassedTests().getAllResults(),
        context.getFailedTests().getAllResults(),
        context.getSkippedTests().getAllResults(),
        context.getFailedButWithinSuccessPercentageTests().getAllResults());
  }
  //
  // implements ITestListener
  /////

  private static String getOutputFile(ITestContext context) {
    return context.getName() + ".html";
  }

  public static void generateTable(
      PrintWriter pw,
      String title,
      Collection<ITestResult> tests,
      String cssClass,
      Comparator<ITestResult> comparator) {
    pw.append("<table width='100%' border='1' class='invocation-")
        .append(cssClass)
        .append("'>\n")
        .append("<tr><td colspan='4' align='center'><b>")
        .append(title)
        .append("</b></td></tr>\n")
        .append("<tr>")
        .append("<td><b>Test method</b></td>\n")
        .append("<td width=\"30%\"><b>Exception</b></td>\n")
        .append("<td width=\"10%\"><b>Time (seconds)</b></td>\n")
        .append("<td><b>Instance</b></td>\n")
        .append("</tr>\n");

    if (tests instanceof List) {
      ((List<ITestResult>) tests).sort(comparator);
    }

    // User output?
    String id;
    Throwable tw;

    for (ITestResult tr : tests) {
      pw.append("<tr>\n");

      // Test method
      ITestNGMethod method = tr.getMethod();

      String name = method.getMethodName();
      pw.append("<td title='")
          .append(tr.getTestClass().getName())
          .append(".")
          .append(name)
          .append("()'>")
          .append("<b>")
          .append(name)
          .append("</b>");

      // Test class
      String testClass = tr.getTestClass().getName();
      if (testClass != null) {
        pw.append("<br>").append("Test class: ").append(testClass);

        // Test name
        String testName = tr.getTestName();
        if (testName != null) {
          pw.append(" (").append(testName).append(")");
        }
      }

      // Method description
      if (!Utils.isStringEmpty(method.getDescription())) {
        pw.append("<br>").append("Test method: ").append(method.getDescription());
      }

      Object[] parameters = tr.getParameters();
      if (parameters != null && parameters.length > 0) {
        pw.append("<br>Parameters: ");
        for (int j = 0; j < parameters.length; j++) {
          if (j > 0) {
            pw.append(", ");
          }
          pw.append(parameters[j] == null ? "null" : parameters[j].toString());
        }
      }

      //
      // Output from the method, created by the user calling Reporter.log()
      //
      {
        List<String> output = Reporter.getOutput(tr);
        if (null != output && output.size() > 0) {
          pw.append("<br/>");
          // Method name
          String divId = "Output-" + tr.hashCode();
          pw.append("\n<a href=\"#")
              .append(divId)
              .append("\"")
              .append(" onClick='toggleBox(\"")
              .append(divId)
              .append("\", this, \"Show output\", \"Hide output\");'>")
              .append("Show output</a>\n")
              .append("\n<a href=\"#")
              .append(divId)
              .append("\"")
              .append(" onClick=\"toggleAllBoxes();\">Show all outputs</a>\n");

          // Method output
          pw.append("<div class='log' id=\"").append(divId).append("\">\n");
          for (String s : output) {
            pw.append(s).append("<br/>\n");
          }
          pw.append("</div>\n");
        }
      }

      pw.append("</td>\n");

      // Exception
      tw = tr.getThrowable();
      String stackTrace;
      String fullStackTrace;

      id = "stack-trace" + tr.hashCode();
      pw.append("<td>");

      if (null != tw) {
        fullStackTrace = Utils.longStackTrace(tw, true);
        stackTrace = "<div><pre>" + Utils.shortStackTrace(tw, true) + "</pre></div>";

        pw.append(stackTrace);
        // JavaScript link
        pw.append("<a href='#' onClick='toggleBox(\"")
            .append(id)
            .append(
                "\", this, \"Click to show all stack frames\", \"Click to hide stack frames\")'>")
            .append("Click to show all stack frames")
            .append("</a>\n")
            .append("<div class='stack-trace' id='")
            .append(id)
            .append("'>")
            .append("<pre>")
            .append(fullStackTrace)
            .append("</pre>")
            .append("</div>");
      }

      pw.append("</td>\n");

      // Time
      long time = (tr.getEndMillis() - tr.getStartMillis()) / 1000;
      String strTime = Long.toString(time);
      pw.append("<td>").append(strTime).append("</td>\n");

      // Instance
      Object instance = tr.getInstance();
      pw.append("<td>").append(Objects.toString(instance)).append("</td>");

      pw.append("</tr>\n");
    }

    pw.append("</table><p>\n");
  }

  private static String arrayToString(String[] array) {
    StringBuilder result = new StringBuilder();
    for (String element : array) {
      result.append(element).append(" ");
    }

    return result.toString();
  }

  private static final String HEAD =
      "\n<style type=\"text/css\">\n"
          + ".log { display: none;} \n"
          + ".stack-trace { display: none;} \n"
          + "</style>\n"
          + "<script type=\"text/javascript\">\n"
          + "<!--\n"
          + "function flip(e) {\n"
          + "  current = e.style.display;\n"
          + "  if (current == 'block') {\n"
          + "    e.style.display = 'none';\n"
          + "    return 0;\n"
          + "  }\n"
          + "  else {\n"
          + "    e.style.display = 'block';\n"
          + "    return 1;\n"
          + "  }\n"
          + "}\n"
          + "\n"
          + "function toggleBox(szDivId, elem, msg1, msg2)\n"
          + "{\n"
          + "  var res = -1;"
          + "  if (document.getElementById) {\n"
          + "    res = flip(document.getElementById(szDivId));\n"
          + "  }\n"
          + "  else if (document.all) {\n"
          + "    // this is the way old msie versions work\n"
          + "    res = flip(document.all[szDivId]);\n"
          + "  }\n"
          + "  if(elem) {\n"
          + "    if(res == 0) elem.innerHTML = msg1; else elem.innerHTML = msg2;\n"
          + "  }\n"
          + "\n"
          + "}\n"
          + "\n"
          + "function toggleAllBoxes() {\n"
          + "  if (document.getElementsByTagName) {\n"
          + "    d = document.getElementsByTagName('div');\n"
          + "    for (i = 0; i < d.length; i++) {\n"
          + "      if (d[i].className == 'log') {\n"
          + "        flip(d[i]);\n"
          + "      }\n"
          + "    }\n"
          + "  }\n"
          + "}\n"
          + "\n"
          + "// -->\n"
          + "</script>\n"
          + "\n";

  public static void generateLog(
      ITestContext testContext,
      String host,
      String outputDirectory,
      Collection<ITestResult> failedConfs,
      Collection<ITestResult> skippedConfs,
      Collection<ITestResult> passedTests,
      Collection<ITestResult> failedTests,
      Collection<ITestResult> skippedTests,
      Collection<ITestResult> percentageTests) {
    try (PrintWriter writer =
        new PrintWriter(Utils.openWriter(outputDirectory, getOutputFile(testContext)))) {

      writer
          .append("<html>\n<head>\n")
          .append("<title>TestNG:  ")
          .append(testContext.getName())
          .append("</title>\n")
          .append(HtmlHelper.getCssString())
          .append(HEAD)
          .append("</head>\n")
          .append("<body>\n");

      Date startDate = testContext.getStartDate();
      Date endDate = testContext.getEndDate();
      long duration = (endDate.getTime() - startDate.getTime()) / 1000;
      int passed =
          testContext.getPassedTests().size()
              + testContext.getFailedButWithinSuccessPercentageTests().size();
      int failed = testContext.getFailedTests().size();
      int skipped = testContext.getSkippedTests().size();
      String hostLine =
          Utils.isStringEmpty(host) ? "" : "<tr><td>Remote host:</td><td>" + host + "</td>\n</tr>";

      writer
          .append("<h2 align='center'>")
          .append(testContext.getName())
          .append("</h2>")
          .append("<table border='1' align=\"center\">\n")
          .append("<tr>\n")
          //    .append("<td>Property
          // file:</td><td>").append(m_testRunner.getPropertyFileName()).append("</td>\n")
          //    .append("</tr><tr>\n")
          .append("<td>Tests passed/Failed/Skipped:</td><td>")
          .append(Integer.toString(passed))
          .append("/")
          .append(Integer.toString(failed))
          .append("/")
          .append(Integer.toString(skipped))
          .append("</td>\n")
          .append("</tr><tr>\n")
          .append("<td>Started on:</td><td>")
          .append(testContext.getStartDate().toString())
          .append("</td>\n")
          .append("</tr>\n")
          .append(hostLine)
          .append("<tr><td>Total time:</td><td>")
          .append(Long.toString(duration))
          .append(" seconds (")
          .append(Long.toString(endDate.getTime() - startDate.getTime()))
          .append(" ms)</td>\n")
          .append("</tr><tr>\n")
          .append("<td>Included groups:</td><td>")
          .append(arrayToString(testContext.getIncludedGroups()))
          .append("</td>\n")
          .append("</tr><tr>\n")
          .append("<td>Excluded groups:</td><td>")
          .append(arrayToString(testContext.getExcludedGroups()))
          .append("</td>\n")
          .append("</tr>\n")
          .append("</table><p/>\n");

      writer.append(
          "<small><i>(Hover the method name to see the test class name)</i></small><p/>\n");
      if (!failedConfs.isEmpty()) {
        generateTable(
            writer, "FAILED CONFIGURATIONS", failedConfs, "failed", CONFIGURATION_COMPARATOR);
      }
      if (!skippedConfs.isEmpty()) {
        generateTable(
            writer, "SKIPPED CONFIGURATIONS", skippedConfs, "skipped", CONFIGURATION_COMPARATOR);
      }
      if (!failedTests.isEmpty()) {
        generateTable(writer, "FAILED TESTS", failedTests, "failed", NAME_COMPARATOR);
      }
      if (!percentageTests.isEmpty()) {
        generateTable(
            writer,
            "FAILED TESTS BUT WITHIN SUCCESS PERCENTAGE",
            percentageTests,
            "percent",
            NAME_COMPARATOR);
      }
      if (!passedTests.isEmpty()) {
        generateTable(writer, "PASSED TESTS", passedTests, "passed", NAME_COMPARATOR);
      }
      if (!skippedTests.isEmpty()) {
        generateTable(writer, "SKIPPED TESTS", skippedTests, "skipped", NAME_COMPARATOR);
      }

      writer.append("</body>\n</html>");
    } catch (IOException e) {
      if (TestRunner.getVerbose() > 1) {
        Logger.getLogger(TestRunner.class).error(e.getMessage(), e);
      } else {
        log(e.getMessage());
      }
    }
  }

  private static void log(String s) {
    Logger.getLogger(TestHTMLReporter.class).info("[TestHTMLReporter] " + s);
  }

  private static class NameComparator implements Comparator<ITestResult> {

    @Override
    public int compare(ITestResult o1, ITestResult o2) {
      String c1 = o1.getMethod().getMethodName();
      String c2 = o2.getMethod().getMethodName();
      return c1.compareTo(c2);
    }
  }

  private static class ConfigurationComparator implements Comparator<ITestResult> {

    @Override
    public int compare(ITestResult o1, ITestResult o2) {
      ITestNGMethod tm1 = o1.getMethod();
      ITestNGMethod tm2 = o2.getMethod();
      return annotationValue(tm2) - annotationValue(tm1);
    }

    private static int annotationValue(ITestNGMethod method) {
      if (method.isBeforeSuiteConfiguration()) {
        return 10;
      }
      if (method.isBeforeTestConfiguration()) {
        return 9;
      }
      if (method.isBeforeClassConfiguration()) {
        return 8;
      }
      if (method.isBeforeGroupsConfiguration()) {
        return 7;
      }
      if (method.isBeforeMethodConfiguration()) {
        return 6;
      }
      if (method.isAfterMethodConfiguration()) {
        return 5;
      }
      if (method.isAfterGroupsConfiguration()) {
        return 4;
      }
      if (method.isAfterClassConfiguration()) {
        return 3;
      }
      if (method.isAfterTestConfiguration()) {
        return 2;
      }
      if (method.isAfterSuiteConfiguration()) {
        return 1;
      }

      return 0;
    }
  }
}
