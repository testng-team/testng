package org.testng.reporters.jq;

import org.testng.ISuite;
import org.testng.internal.Utils;
import org.testng.reporters.XMLStringBuffer;

public class TestNgXmlPanel extends BaseMultiSuitePanel {

  public TestNgXmlPanel(Model model) {
    super(model);
  }


  @Override
  public String getPrefix() {
    return "test-xml-";
  }

  @Override
  public String getHeader(ISuite suite) {
    return suite.getXmlSuite().getFileName();
  }

  @Override
  public String getContent(ISuite suite, XMLStringBuffer main) {
    XMLStringBuffer xsb = new XMLStringBuffer(main.getCurrentIndent());
    xsb.push("pre");
    xsb.addString(Utils.escapeHtml(suite.getXmlSuite().toXml()));
    xsb.pop("pre");
    return xsb.toXML();
  }

  @Override
  public String getNavigatorLink(ISuite suite) {
    String fqName = suite.getXmlSuite().getFileName();
    if (fqName == null) fqName = "/[unset file name]";
    return fqName.substring(fqName.lastIndexOf("/") + 1);
  }

}
