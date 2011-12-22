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

  public static String getTag() {
    return "test-0";
  }

  @Override
  public void generate(XMLStringBuffer xsb) {
    xsb.push(D, C, "panel " + getTag());
    for (ISuite s : getSuites()) {
      xsb.push("ul");
      xsb.addOptional("li", s.getName());
      for (XmlTest test : s.getXmlSuite().getTests()) {
        xsb.push("ul");
        xsb.addOptional("li", test.getName());
        xsb.pop("ul");
      }
      xsb.pop("ul");
    }
    xsb.pop(D);
  }
}
