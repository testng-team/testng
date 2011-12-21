package org.testng.reporters.jq;

import static org.testng.reporters.jq.Main.C;
import static org.testng.reporters.jq.Main.D;

import org.testng.ISuite;
import org.testng.reporters.XMLStringBuffer;
import org.testng.xml.XmlTest;

import java.util.List;

/**
 * Display the list of <test> tags.
 */
public class TestPanel {

  public static String getTag() {
    return "test-0";
  }

  public void generate(List<ISuite> suites, XMLStringBuffer xsb) {
    xsb.push(D, C, "panel " + getTag());
    for (ISuite s : suites) {
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
