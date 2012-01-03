package org.testng.reporters.jq;

import org.testng.ISuite;
import org.testng.reporters.XMLStringBuffer;

public interface IPanel {
  void generate(XMLStringBuffer xsb);
  String getNavigatorLink(ISuite suite);
}
