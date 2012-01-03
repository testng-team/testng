package org.testng.reporters.jq;

import org.testng.ISuite;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.reporters.XMLStringBuffer;

import java.util.List;

/**
 * Display the reporter output for each test result.
 */
public class ReporterPanel extends BaseMultiSuitePanel {

  public ReporterPanel(Model model) {
    super(model);
  }


  @Override
  public String getPrefix() {
    return "reporter-";
  }

  @Override
  public String getHeader(ISuite suite) {
    return "Reporter output for " + suite.getName();
  }

  @Override
  public String getContent(ISuite suite, XMLStringBuffer main) {
    XMLStringBuffer xsb = new XMLStringBuffer(main.getCurrentIndent());
    for (ITestResult tr : getModel().getAllTestResults(suite)) {
      List<String> lines = Reporter.getOutput(tr);
      if (! lines.isEmpty()) {
        xsb.push(D, C, "reporter-method-div");
        xsb.addRequired(S, Model.getTestResultName(tr), C, "reporter-method-name");
        xsb.push(D, C, "reporter-method-output-div");
        for (String output : lines) {
          xsb.addRequired(S, output, C, "reporter-method-output");
        }
        xsb.pop(D);
        xsb.pop(D);
      }
    }
    return xsb.toXML();
  }

  @Override
  public String getNavigatorLink(ISuite suite) {
    return "Reporter output";
  }

}
