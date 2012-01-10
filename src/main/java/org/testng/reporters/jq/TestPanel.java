package org.testng.reporters.jq;

import org.testng.ISuite;
import org.testng.reporters.XMLStringBuffer;
import org.testng.xml.XmlTest;

/**
 * Display the list of <test> tags.
 */
public class TestPanel extends BaseMultiSuitePanel {

  public TestPanel(Model model) {
    super(model);
  }


  @Override
  public String getPrefix() {
    return "testlist-";
  }

  @Override
  public String getHeader(ISuite suite) {
    return "Tests for " + suite.getName();
  }

  @Override
  public String getContent(ISuite suite, XMLStringBuffer main) {
    XMLStringBuffer xsb = new XMLStringBuffer(main.getCurrentIndent());

    xsb.push("ul");
    for (XmlTest test : suite.getXmlSuite().getTests()) {
      xsb.push("li");
      int count = test.getXmlClasses().size();
      String name = test.getName() + " (" + pluralize(count, "class") + ")";
      xsb.addRequired(S, name, C, "test-name");
      xsb.pop("li");
    }
    xsb.pop("ul");

    return xsb.toXML();
  }

  @Override
  public String getNavigatorLink(ISuite suite) {
    return pluralize(suite.getXmlSuite().getTests().size(), "test");
  }

  @Override
  public String getClassName() {
    return "test-stats";
  }
}
