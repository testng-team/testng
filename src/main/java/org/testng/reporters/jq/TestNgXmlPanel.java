package org.testng.reporters.jq;

import org.testng.ISuite;
import org.testng.internal.Utils;
import org.testng.reporters.XMLStringBuffer;

public class TestNgXmlPanel extends BasePanel {

  public TestNgXmlPanel(Model model) {
    super(model);
  }

  public static String getTag(int count) {
    return "test-xml-" + count;
  }

  @Override
  public void generate(XMLStringBuffer xsb) {
    int counter = 0;
    for (ISuite s : getSuites()) {
      xsb.push(D, C, "panel", "panel-name", getTag(counter));
      xsb.push(D, C, "main-panel-header rounded-window-top");
      xsb.addOptional(S, s.getXmlSuite().getFileName(),
          C, "header-content");
      xsb.pop(D);

      xsb.push(D, C, "main-panel-content rounded-window-bottom");
      xsb.push("pre");
      xsb.addString(Utils.escapeHtml(s.getXmlSuite().toXml()));
      xsb.pop("pre");
      xsb.pop(D);

      xsb.pop(D);

      counter++;
    }
  }

}
