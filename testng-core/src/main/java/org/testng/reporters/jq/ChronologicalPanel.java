package org.testng.reporters.jq;

import java.util.List;
import org.testng.IInvokedMethod;
import org.testng.ISuite;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.reporters.XMLStringBuffer;

public class ChronologicalPanel extends BaseMultiSuitePanel {

  public ChronologicalPanel(Model model) {
    super(model);
  }

  @Override
  public String getPrefix() {
    return "chronological-";
  }

  @Override
  public String getHeader(ISuite suite) {
    return "Methods in chronological order";
  }

  @Override
  public String getContent(ISuite suite, XMLStringBuffer main) {
    XMLStringBuffer xsb = new XMLStringBuffer(main.getCurrentIndent());
    List<IInvokedMethod> invokedMethods = suite.getAllInvokedMethods();

    invokedMethods.sort(
        (m1, m2) ->
            (int) (m1.getTestResult().getStartMillis() - m2.getTestResult().getStartMillis()));

    String currentClass = "";
    long start = 0;
    for (IInvokedMethod im : invokedMethods) {
      ITestNGMethod m = im.getTestMethod();
      String cls = extractMethodType(m);
      ITestResult tr = im.getTestResult();
      String methodName = Model.getTestResultName(tr);

      if (!m.getTestClass().getName().equals(currentClass)) {
        if (!"".equals(currentClass)) {
          xsb.pop(D);
        }
        xsb.push(D, C, "chronological-class");
        xsb.addRequired(D, m.getTestClass().getName(), C, "chronological-class-name");
        currentClass = m.getTestClass().getName();
      }
      xsb.push(D, C, cls);
      if (tr.getStatus() == ITestResult.FAILURE) {
        xsb.push("img", "src", Model.getImage("failed"));
        xsb.pop("img");
      }

      // No need to check for skipped methods since by definition, they were never
      // invoked.

      xsb.addRequired(S, methodName, C, "method-name");
      if (start == 0) {
        start = tr.getStartMillis();
      }
      xsb.addRequired(S, tr.getStartMillis() - start + " ms", C, "method-start");
      xsb.pop(D);
    }
    return xsb.toXML();
  }

  private static String extractMethodType(ITestNGMethod m) {
    String cls = "test-method";
    if (m.isBeforeSuiteConfiguration()) {
      cls = "configuration-suite before";
    } else if (m.isAfterSuiteConfiguration()) {
      cls = "configuration-suite after";
    } else if (m.isBeforeTestConfiguration()) {
      cls = "configuration-test before";
    } else if (m.isAfterTestConfiguration()) {
      cls = "configuration-test after";
    } else if (m.isBeforeClassConfiguration()) {
      cls = "configuration-class before";
    } else if (m.isAfterClassConfiguration()) {
      cls = "configuration-class after";
    } else if (m.isBeforeMethodConfiguration()) {
      cls = "configuration-method before";
    } else if (m.isAfterMethodConfiguration()) {
      cls = "configuration-method after";
    }
    return cls;
  }

  @Override
  public String getNavigatorLink(ISuite suite) {
    return "Chronological view";
  }
}
