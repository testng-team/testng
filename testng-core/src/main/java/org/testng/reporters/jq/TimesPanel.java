package org.testng.reporters.jq;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.collections.Maps;
import org.testng.reporters.XMLStringBuffer;
import org.testng.xml.XmlSuite;

public class TimesPanel extends BaseMultiSuitePanel {
  private Map<String, Long> m_totalTime = Maps.newHashMap();

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

  private String js(ISuite suite) {
    String functionName = "tableData_" + suiteToTag(suite);
    StringBuilder result =
        new StringBuilder(
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
                + "data.addColumn('number', 'Time (ms)');\n");

    List<ITestResult> allTestResults = getModel().getAllTestResults(suite);
    result.append("data.addRows(").append(allTestResults.size()).append(");\n");

    allTestResults.sort((o1, o2) -> (int) (time(o2) - time(o1)));

    int index = 0;
    for (ITestResult tr : allTestResults) {
      ITestNGMethod m = tr.getMethod();
      long time = tr.getEndMillis() - tr.getStartMillis();
      result
          .append("data.setCell(")
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
          .append(m.getTestClass().getName())
          .append("')\n")
          .append("data.setCell(")
          .append(index)
          .append(", 3, ")
          .append(time)
          .append(");\n");
      Long total = m_totalTime.get(suite.getName());
      if (total == null) {
        total = 0L;
      }
      m_totalTime.put(suite.getName(), total + time);
      index++;
    }

    result
        .append("window.suiteTableData['")
        .append(suiteToTag(suite))
        .append("']")
        .append("= { tableData: data, tableDiv: 'times-div-")
        .append(suiteToTag(suite))
        .append("'}\n")
        .append("return data;\n")
        .append("}\n");

    return result.toString();
  }

  @Override
  public String getContent(ISuite suite, XMLStringBuffer main) {
    XMLStringBuffer xsb = new XMLStringBuffer(main.getCurrentIndent());
    xsb.push(D, C, "times-div");
    xsb.push("script", "type", "text/javascript");
    xsb.addString(js(suite));
    xsb.pop("script");
    long time = maxTime(suite);
    xsb.addRequired(
        S, String.format("Total running time: %s", prettyDuration(time)), C, "suite-total-time");
    xsb.push(D, "id", "times-div-" + suiteToTag(suite));
    xsb.pop(D);
    xsb.pop(D);
    return xsb.toXML();
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
    Optional<ITestContext> maxValue =
        suite.getResults().values().stream()
            .map(ISuiteResult::getTestContext)
            .max(Comparator.comparing(TimesPanel::time));
    if (maxValue.isPresent()) {
      return time(maxValue.get());
    }
    return result;
  }

  private static Long time(ITestContext ctx) {
    return ctx.getEndDate().getTime() - ctx.getStartDate().getTime();
  }
}
