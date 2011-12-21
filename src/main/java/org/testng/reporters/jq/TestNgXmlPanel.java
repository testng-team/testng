package org.testng.reporters.jq;

import static org.testng.reporters.jq.Main.C;
import static org.testng.reporters.jq.Main.D;
import static org.testng.reporters.jq.Main.S;

import org.testng.ISuite;
import org.testng.internal.Utils;
import org.testng.reporters.XMLStringBuffer;

import java.util.List;

public class TestNgXmlPanel implements IPanel {

  public static String getTag(int count) {
    return "test-xml-" + count;
  }

  @Override
  public void generate(List<ISuite> suites, XMLStringBuffer xsb) {
    int counter = 0;
    for (ISuite s : suites) {
      xsb.push(D, C, "panel " + getTag(counter));
      xsb.push(D, C, "testng-xml-panel header rounded-window-top");
      xsb.addOptional(S, s.getXmlSuite().getFileName(),
          C, "header-content");
      xsb.pop(D);

      xsb.push(D, C, "testng-xml-panel content rounded-window-bottom");
      xsb.push("pre");
      xsb.addString(Utils.escapeHtml(s.getXmlSuite().toXml()));
      xsb.pop("pre");
      xsb.pop(D);

      xsb.pop(D);

      counter++;
    }
  }

}
