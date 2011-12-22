package org.testng.reporters.jq;

import org.testng.ISuite;
import org.testng.reporters.XMLStringBuffer;
import org.testng.xml.XmlTest;

/**
 * Display the list of <test> tags.
 */
public class TestPanel extends BasePanel {

  public TestPanel(Model model) {
    super(model);
  }

  public static String getTag(ISuite suite) {
    return "testlist-" + suite.getName().replace(" ", "_");
  }

  @Override
  public void generate(XMLStringBuffer xsb) {
    for (ISuite s : getSuites()) {
      xsb.push(D, C, "panel", "panel-name", getTag(s));

      xsb.push(D, C, "main-panel-header rounded-window-top");
      xsb.addOptional(S, "Tests for " + s.getName(),
          C, "header-content");
      xsb.pop(D);

      xsb.push(D, C, "main-panel-content rounded-window-bottom");
      xsb.push("ul");
      for (XmlTest test : s.getXmlSuite().getTests()) {
        xsb.push("li");
        int count = test.getXmlClasses().size();
        String name = test.getName() + " (" + pluralize(count, "class") + ")";
        xsb.addRequired(S, name, C, "test-name");
        xsb.pop("li");
      }
      xsb.pop("ul");
      xsb.pop(D);

      xsb.pop(D);
    }
  }
}
