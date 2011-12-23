package org.testng.reporters.jq;

import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.reporters.XMLStringBuffer;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class NavigatorPanel extends BasePanel {

  private TestNgXmlPanel m_testNgPanel;
  private TestPanel m_testPanel;

  public NavigatorPanel(Model model, TestNgXmlPanel testNgPanel, TestPanel testPanel) {
    super(model);
    m_testNgPanel = testNgPanel;
    m_testPanel = testPanel;
  }

  @Override
  public void generate(XMLStringBuffer main) {
    main.push(D, C, "navigator-root");
    int suiteCount = 0;
    for (ISuite suite : getSuites()) {
      if (suite.getResults().size() == 0) {
        continue;
      }

      String suiteName = "suite-" + suiteCount;

      XMLStringBuffer header = new XMLStringBuffer(main.getCurrentIndent());

      Map<String, ISuiteResult> results = suite.getResults();
      int failed = 0;
      int skipped = 0;
      int passed = 0;
      for (ISuiteResult result : results.values()) {
        ITestContext context = result.getTestContext();
        failed += context.getFailedTests().size();
        skipped += context.getSkippedTests().size();
        passed += context.getPassedTests().size();
      }

      // Suite name in big font
      header.push(D, C, "suite");
      header.push(D, C, "suite-header rounded-window");
      // Extra div so the highlighting logic will only highlight this line and not
      // the entire container
      header.push(D, C, "light-rounded-window-top");
      header.push("a", "href", "#",
          "panel-name", suiteName,
          C, "navigator-link");
      header.addOptional(S, suite.getName(), C, "suite-name");
      header.pop("a");
      header.pop(D);

      header.push(D, C, "navigator-suite-content");

      //
      // Info
      //
      header.push(D, C, "suite-section-title");
      header.addRequired(S, "Info");
      header.pop(D);

      // Info content
      header.push(D, C, "suite-section-content");
      int total = failed + skipped + passed;
      String stats = String.format("%s, %s %s %s",
          pluralize(total, "method"),
          maybe(failed, "failed", ", "),
          maybe(skipped, "skipped", ", "),
          maybe(passed, "passed", ""));

      header.push("ul");

      // Tests
      header.push("li");
      header.push("a", "href", "#",
          "panel-name", m_testPanel.getPanelName(suite),
          C, "navigator-link ");
      header.addOptional(S, String.format("%s ", pluralize(results.values().size(), "test"),
          C, "test-stats"));
      header.pop("a");
      header.pop("li");

      // testng.xml
      header.push("li");
      header.push("a", "href", "#",
          "panel-name", m_testNgPanel.getPanelName(suite),
          C, "navigator-link");
      String fqName = suite.getXmlSuite().getFileName();
      header.addOptional(S, fqName.substring(fqName.lastIndexOf("/") + 1),
          C, "testng-xml");
      header.pop("a");
      header.pop("li");

      header.pop("ul");
      header.pop(D); // suite-section-content

      //
      // Methods
      //
      header.push(D, C, "result-section");

      header.push(D, C, "suite-section-title");
      header.addRequired(S, "Results");
      header.pop(D);

      // Method stats
      header.push(D, C, "suite-section-content");
      header.push("ul");
      header.push("li");
      header.addOptional(S, stats, C, "method-stats");
      header.pop("li");

      generateMethodList("Failed methods", ITestResult.FAILURE, Main.getImage("failed"),
          suite, suiteName, header);
      generateMethodList("Skipped methods", ITestResult.SKIP, Main.getImage("skipped"),
          suite, suiteName, header);

      header.pop("ul");

      header.pop(D); // suite-section-content
      header.pop(D); // suite-header
      header.pop(D); // suite

      header.pop(D); // result-section

      header.pop(D); // navigator-suite-content

      main.addString(header.toXML());

      suiteCount++;
    }
    main.pop(D);
  }

  private static String maybe(int count, String s, String sep) {
    return count > 0 ? count + " " + s + sep: "";
  }

  private void generateMethodList(String name, int status, String image, ISuite suite,
      String suiteName, XMLStringBuffer main) {
    XMLStringBuffer xsb = new XMLStringBuffer(main.getCurrentIndent());

    xsb.push("li");
    // Failed methods
    xsb.addRequired(S, name, C, "method-list-title");

    // List of methods
    xsb.push(D, C, "method-list-content");
    int count = 0;
    List<ITestResult> testResults = getModel().getTestResults(suite);
    Collections.sort(testResults, ResultsByClass.METHOD_NAME_COMPARATOR);
    for (ITestResult tr : testResults) {
      if (tr.getStatus() == status) {
        String testName = Model.getTestResultName(tr);
        xsb.push(S);
        xsb.addEmptyElement("img", "src", image, "width", "3%");
        xsb.addRequired("a", testName, "href", "#",
            "hash-for-method", getModel().getTag(tr),
            "panel-name", suiteName,
            C, "method navigator-link");
        xsb.pop(S);
        xsb.addEmptyElement("br");
        count++;
      }
    }
    xsb.pop(D);
    xsb.pop("li");

    if (count > 0) {
      main.addString(xsb.toXML());
    }
  }

}
