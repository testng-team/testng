package org.testng.reporters.jq;

import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.collections.Lists;
import org.testng.reporters.XMLStringBuffer;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class NavigatorPanel extends BasePanel {

  private List<INavigatorPanel> m_panels;

  public NavigatorPanel(Model model, List<INavigatorPanel> panels) {
    super(model);
    m_panels = panels;
  }

  @Override
  public void generate(XMLStringBuffer main) {
    main.push(D, C, "navigator-root");
    main.push(D, C, "navigator-suite-header");
    main.addRequired(S, "All suites");
    main.push("a", C, "collapse-all-link", "href", "#", "title", "Collapse/expand all the suites");
    main.push("img", "src", "collapseall.gif", C, "collapse-all-icon");
    main.pop("img");
    main.pop("a");
    main.pop(D);
    for (ISuite suite : getSuites()) {
      if (suite.getResults().size() == 0) {
        continue;
      }

      String suiteName = "suite-" + suiteToTag(suite);

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
      header.push(D, C, "rounded-window");
      // Extra div so the highlighting logic will only highlight this line and not
      // the entire container
      header.push(D, C, "suite-header light-rounded-window-top");
      header.push("a", "href", "#",
          "panel-name", suiteName,
          C, "navigator-link");
      header.addOptional(S, suite.getName(),
          C, "suite-name border-" + getModel().getStatusForSuite(suite.getName()));
      header.pop("a");
      header.pop(D);

      header.push(D, C, "navigator-suite-content");

      generateInfo(header, suite);
      generateResult(header, failed, skipped, passed, suite, suiteName);

      header.pop("ul");

      header.pop(D); // suite-section-content
      header.pop(D); // suite-header
      header.pop(D); // suite

      header.pop(D); // result-section

      header.pop(D); // navigator-suite-content

      main.addString(header.toXML());
    }
    main.pop(D);
  }

  private void generateResult(XMLStringBuffer header, int failed, int skipped, int passed,
      ISuite suite, String suiteName) {
    //
    // Results
    //
    header.push(D, C, "result-section");

    header.push(D, C, "suite-section-title");
    header.addRequired(S, "Results");
    header.pop(D);

    // Method stats
    int total = failed + skipped + passed;
    String stats = String.format("%s, %s %s %s",
        pluralize(total, "method"),
        maybe(failed, "failed", ", "),
        maybe(skipped, "skipped", ", "),
        maybe(passed, "passed", ""));
    header.push(D, C, "suite-section-content");
    header.push("ul");
    header.push("li");
    header.addOptional(S, stats, C, "method-stats");
    header.pop("li");

    generateMethodList("Failed methods", new ResultsByStatus(suite, "failed", ITestResult.FAILURE),
        suiteName, header);
    generateMethodList("Skipped methods", new ResultsByStatus(suite, "skipped", ITestResult.SKIP),
        suiteName, header);
    generateMethodList("Passed methods", new ResultsByStatus(suite, "passed", ITestResult.SUCCESS),
        suiteName, header);
    }

  private void generateInfo(XMLStringBuffer header, ISuite suite) {
    //
    // Info
    //
    header.push(D, C, "suite-section-title");
    header.addRequired(S, "Info");
    header.pop(D);

    header.push(D, C, "suite-section-content");

    header.push("ul");

    // All the panels
    for (INavigatorPanel panel : m_panels) {
      addLinkTo(header, panel, suite);
    }

    header.pop("ul");
    header.pop(D); // suite-section-content
  }

  private void addLinkTo(XMLStringBuffer header, INavigatorPanel panel, ISuite suite) {
    String text = panel.getNavigatorLink(suite);
    header.push("li");
    header.push("a", "href", "#",
        "panel-name", panel.getPanelName(suite),
        C, "navigator-link ");
    String className = panel.getClassName();
    if (className != null) {
      header.addOptional(S, text, C, className);
    } else {
      header.addOptional(S, text);
    }
    header.pop("a");
    header.pop("li");
  }

  private static String maybe(int count, String s, String sep) {
    return count > 0 ? count + " " + s + sep: "";
  }

  private List<ITestResult> getMethodsByStatus(ISuite suite, int status) {
    List<ITestResult> result = Lists.newArrayList();
    List<ITestResult> testResults = getModel().getTestResults(suite);
    for (ITestResult tr : testResults) {
      if (tr.getStatus() == status) {
        result.add(tr);
      }
    }
    Collections.sort(result, ResultsByClass.METHOD_NAME_COMPARATOR);

    return result;
  }

  private static interface IResultProvider {
    List<ITestResult> getResults();
    String getType();
  }

  private abstract static class BaseResultProvider implements IResultProvider {
    protected ISuite m_suite;
    protected String m_type;
    public BaseResultProvider(ISuite suite, String type) {
      m_suite = suite;
      m_type = type;
    }

    @Override
    public String getType() {
      return m_type;
    }
  }

  private class ResultsByStatus extends BaseResultProvider {
    private final int m_status;

    public ResultsByStatus(ISuite suite, String type, int status) {
      super(suite, type);
      m_status = status;
    }

    @Override
    public List<ITestResult> getResults() {
      return getMethodsByStatus(m_suite, m_status);
    }
  }

  private void generateMethodList(String name, IResultProvider provider,
      String suiteName, XMLStringBuffer main) {
    XMLStringBuffer xsb = new XMLStringBuffer(main.getCurrentIndent());
    String type = provider.getType();
    String image = Model.getImage(type);

    xsb.push("li");

    // The methods themselves
    xsb.addRequired(S, name, C, "method-list-title " + type);

    // The mark up to show the (hide)/(show) links
    xsb.push(S, C, "show-or-hide-methods " + type);
    xsb.addRequired("a", " (hide)", "href", "#", C, "hide-methods " + type + " " + suiteName,
        "panel-name", suiteName);
    xsb.addRequired("a", " (show)", "href", "#",C, "show-methods " + type + " " + suiteName,
        "panel-name", suiteName);
    xsb.pop(S);

    // List of methods
    xsb.push(D, C, "method-list-content " + type + " " + suiteName);
    int count = 0;
    List<ITestResult> testResults = provider.getResults();
    if (testResults != null) {
      Collections.sort(testResults, ResultsByClass.METHOD_NAME_COMPARATOR);
      for (ITestResult tr : testResults) {
        String testName = Model.getTestResultName(tr);
        xsb.push(S);
        xsb.addEmptyElement("img", "src", image, "width", "3%");
        xsb.addRequired("a", testName, "href", "#",
            "hash-for-method", getModel().getTag(tr),
            "panel-name", suiteName,
            "title", tr.getTestClass().getName(),
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
