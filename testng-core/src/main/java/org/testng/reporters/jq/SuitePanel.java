package org.testng.reporters.jq;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.testng.ISuite;
import org.testng.ITestResult;
import org.testng.annotations.CustomAttribute;
import org.testng.internal.Utils;
import org.testng.reporters.XMLStringBuffer;
import org.testng.util.Strings;

public class SuitePanel extends BasePanel {

  private static final String PASSED = "passed";
  private static final String SKIPPED = "skipped";
  private static final String FAILED = "failed";

  public SuitePanel(Model model) {
    super(model);
  }

  @Override
  public void generate(XMLStringBuffer xsb) {
    for (ISuite suite : getSuites()) {
      generateSuitePanel(suite, xsb);
    }
  }

  private void generateSuitePanel(ISuite suite, XMLStringBuffer xsb) {
    String divName = suiteToTag(suite);
    xsb.push(D, C, "panel " + divName, "panel-name", "suite-" + divName);
    String[] statuses = new String[] {FAILED, SKIPPED, PASSED};
    ResultsByClass[] results =
        new ResultsByClass[] {
          getModel().getFailedResultsByClass(suite),
          getModel().getSkippedResultsByClass(suite),
          getModel().getPassedResultsByClass(suite),
        };

    for (int i = 0; i < results.length; i++) {
      ResultsByClass byClass = results[i];
      for (Class<?> c : byClass.getClasses()) {
        generateClassPanel(c, byClass.getResults(c), xsb, statuses[i], suite);
      }
    }
    xsb.pop(D);
  }

  private void generateClassPanel(
      Class<?> c, List<ITestResult> results, XMLStringBuffer xsb, String status, ISuite suite) {
    xsb.push(D, C, "suite-" + suiteToTag(suite) + "-class-" + status);
    xsb.push(D, C, "main-panel-header rounded-window-top");

    // Passed/failed icon
    xsb.addEmptyElement("img", "src", Model.getImage(status));
    xsb.addOptional(S, c.getName(), C, "class-name");
    xsb.pop(D);

    xsb.push(D, C, "main-panel-content rounded-window-bottom");

    for (ITestResult tr : results) {
      generateMethod(tr, xsb);
    }
    xsb.pop(D);
    xsb.pop(D);
  }

  private void generateMethod(ITestResult tr, XMLStringBuffer xsb) {
    xsb.push(D, C, "method");
    xsb.push(D, C, "method-content");
    xsb.push("a", "name", Model.getTestResultName(tr));
    xsb.pop("a");
    xsb.addOptional(S, tr.getMethod().getMethodName(), C, "method-name");

    // Parameters?
    if (tr.getParameters().length > 0) {
      String text =
          Arrays.stream(tr.getParameters()).map(Utils::toString).collect(Collectors.joining(","));
      xsb.addOptional(S, "(" + text + ")", C, "parameters");
    }

    CustomAttribute[] attributes = tr.getMethod().getAttributes();
    if (attributes != null && attributes.length > 0) {
      String text =
          Arrays.stream(attributes)
              .map(
                  attribute ->
                      "{ " + attribute.name() + ", " + Arrays.toString(attribute.values()) + " }")
              .collect(Collectors.joining(", "));
      xsb.addOptional(S, Utils.escapeHtml("(" + text + ")"), C, "parameters");
    }

    // Exception?
    if (tr.getStatus() != ITestResult.SUCCESS && tr.getThrowable() != null) {
      xsb.addOptional(D, Utils.shortStackTrace(tr.getThrowable(), true) + "\n", C, "stack-trace");
    }

    // Description?
    String description = tr.getMethod().getDescription();
    if (!Strings.isNullOrEmpty(description)) {
      xsb.push("em");
      xsb.addString("(" + description + ")");
      xsb.pop("em");
    }

    xsb.pop(D);
    xsb.pop(D);
  }
}
