package org.testng.reporters.jq;

import org.testng.ISuite;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.reporters.XMLStringBuffer;

import java.util.List;

public class TimesPanel extends BaseMultiSuitePanel {
  public TimesPanel(Model model) {
    super(model);
  }

  private static String getTag(ISuite suite) {
    return "times-" + suiteToTag(suite);
  }

  @Override
  public String getHeader(ISuite suite) {
    return "Times for " + suite.getName();
  }

  @Override
  public String getPanelName(ISuite suite) {
    return getTag(suite);
  }

  private String js(ISuite suite) {
    String functionName = "tableData_" + suiteToTag(suite);
    StringBuilder result = new StringBuilder(
        "suiteTableInitFunctions.push('" + functionName + "');\n"
          + "function " + functionName + "() {\n"
          + "var data = new google.visualization.DataTable();\n"
          + "data.addColumn('string', 'Method');\n"
          + "data.addColumn('string', 'Class');\n"
          + "data.addColumn('number', 'Time (ms)');\n");

    List<ITestResult> allTestResults = getModel().getAllTestResults(suite);
    result.append(
      "data.addRows(" + allTestResults.size() + ");\n");

    int index = 0;
    for (ITestResult tr : allTestResults) {
      ITestNGMethod m = tr.getMethod();
      long time = tr.getEndMillis() - tr.getStartMillis();
      result.append("data.setCell(" + index + ", "
              + "0, '" + m.getMethodName() + "')\n")
          .append("data.setCell(" + index + ", "
              + "1, '" + m.getTestClass().getName() + "')\n")
          .append("data.setCell(" + index + ", "
              + "2, " + time + ");\n");
      index++;
    }

    result.append(
        "window.suiteTableData['" + suiteToTag(suite) + "']" +
        		"= { tableData: data, tableDiv: 'times-div-" + suiteToTag(suite) + "'}\n"
        + "return data;\n" +
        "}\n");

    return result.toString();
  }

  @Override
  public String getContent(ISuite suite, XMLStringBuffer main) {
    XMLStringBuffer xsb = new XMLStringBuffer(main.getCurrentIndent());
    xsb.push("script", "type", "text/javascript");
    xsb.addString(js(suite));
    xsb.pop("script");
    xsb.push(D, "id", "times-div-" + suiteToTag(suite));
    xsb.pop(D);
    return xsb.toXML();
  }
}
