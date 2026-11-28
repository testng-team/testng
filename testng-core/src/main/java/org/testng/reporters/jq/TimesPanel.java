package org.testng.reporters.jq;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.internal.Utils;
import org.testng.reporters.XMLStringBuffer;
import org.testng.xml.XmlSuite;

public class TimesPanel extends BaseMultiSuitePanel {
  private Map<String, Long> m_totalTime = new HashMap<>();

  public TimesPanel(Model model) {
    super(model);
  }

  @Override
  public String getPrefix() {
    return "times-";
  }

  @Override
  public String getHeader(ISuite suite) {
    return "Times for " + suite.getName();
  }

  /**
   * Writes the table of one suite into {@code xsb}, a row at a time.
   *
   * <p>Not as one String: this table holds four {@code data.setCell} calls per result of the whole
   * suite, and building it whole made it the largest single object of the report generation --
   * larger, at several tens of thousands of results, than the panel buffer that is spilled to disk
   * around it.
   */
  private void js(ISuite suite, XMLStringBuffer xsb) {
    String functionName = "tableData_" + suiteToTag(suite);
    List<ITestResult> allTestResults = getModel().getAllTestResults(suite);
    allTestResults.sort((o1, o2) -> (int) (time(o2) - time(o1)));

    xsb.addString(
        "suiteTableInitFunctions.push('"
            + functionName
            + "');\n"
            + "function "
            + functionName
            + "() {\n"
            + "var data = new google.visualization.DataTable();\n"
            + "data.addColumn('number', 'Number');\n"
            + "data.addColumn('string', 'Method');\n"
            + "data.addColumn('string', 'Class');\n"
            + "data.addColumn('number', 'Time (ms)');\n"
            + "data.addRows("
            + allTestResults.size()
            + ");\n");

    int index = 0;
    // One builder, reused: a row is about 160 characters, so a fresh default-capacity one grows
    // through four array copies before it fits.
    StringBuilder row = new StringBuilder(256);
    for (ITestResult tr : allTestResults) {
      ITestNGMethod m = tr.getMethod();
      long time = tr.getEndMillis() - tr.getStartMillis();
      row.setLength(0);
      row.append("data.setCell(")
          .append(index)
          .append(", 0, ")
          .append(index)
          .append(")\n")
          .append("data.setCell(")
          .append(index)
          .append(", 1, '")
          .append(m.getMethodName())
          .append("')\n")
          .append("data.setCell(")
          .append(index)
          .append(", 2, '")
          .append(Utils.requireTestClassOf(m).getName())
          .append("')\n")
          .append("data.setCell(")
          .append(index)
          .append(", 3, ")
          .append(time)
          .append(");\n");
      xsb.addString(row.toString());
      index++;
    }
    if (!allTestResults.isEmpty()) {
      // Assigned rather than added to: getContent is public and this method used to add each
      // duration again on every call, so a second call reported twice the suite's running time.
      // A suite that started nothing still records nothing, which is what maxTime reads as zero.
      m_totalTime.put(suite.getName(), runningTime(allTestResults));
    }

    xsb.addString(
        "window.suiteTableData['"
            + suiteToTag(suite)
            + "']"
            + "= { tableData: data, tableDiv: 'times-div-"
            + suiteToTag(suite)
            + "'}\n"
            + "return data;\n"
            + "}\n");
  }

  @Override
  void writeContent(ISuite suite, XMLStringBuffer xsb) {
    xsb.push(D, C, "times-div");
    xsb.push("script", "type", "text/javascript");
    js(suite, xsb);
    xsb.pop("script");
    long time = maxTime(suite);
    xsb.addRequired(
        S, String.format("Total running time: %s", prettyDuration(time)), C, "suite-total-time");
    xsb.push(D, "id", "times-div-" + suiteToTag(suite));
    xsb.pop(D);
    xsb.pop(D);
  }

  private String prettyDuration(long totalTime) {
    String result;
    if (totalTime < 1000) {
      result = totalTime + " ms";
    } else if (totalTime < 1000 * 60) {
      result = (totalTime / 1000) + " seconds";
    } else if (totalTime < 1000 * 60 * 60) {
      result = (totalTime / 1000 / 60) + " minutes";
    } else {
      result = (totalTime / 1000 / 60 / 60) + " hours";
    }
    return result;
  }

  @Override
  public String getNavigatorLink(ISuite suite) {
    return "Times";
  }

  private static long time(ITestResult o1) {
    return o1.getEndMillis() - o1.getStartMillis();
  }

  /**
   * The time during which at least one of the results was running. Results that overlap, as they do
   * when a suite runs classes, methods or instances in parallel, count once. Results that run one
   * after another never overlap, so for them this is the sum of their durations. See GITHUB-1423.
   */
  private static long runningTime(List<ITestResult> results) {
    List<ITestResult> byStart = new ArrayList<>(results);
    byStart.sort(Comparator.comparingLong(ITestResult::getStartMillis));
    long total = 0;
    long spanStart = byStart.get(0).getStartMillis();
    long spanEnd = byStart.get(0).getEndMillis();
    for (ITestResult result : byStart.subList(1, byStart.size())) {
      if (result.getStartMillis() > spanEnd) {
        total += spanEnd - spanStart;
        spanStart = result.getStartMillis();
        spanEnd = result.getEndMillis();
      } else {
        spanEnd = Math.max(spanEnd, result.getEndMillis());
      }
    }
    return total + (spanEnd - spanStart);
  }

  private long maxTime(ISuite suite) {
    boolean testsInParallel = XmlSuite.ParallelMode.TESTS.equals(suite.getXmlSuite().getParallel());
    Long result = m_totalTime.get(suite.getName());
    // there are no running tests in the suite
    if (result == null) {
      return 0L;
    }
    if (!testsInParallel) {
      return result;
    }
    return suite.getResults().values().stream()
        .map(ISuiteResult::getTestContext)
        .mapToLong(Utils::durationOf)
        .max()
        .orElse(result);
  }
}
