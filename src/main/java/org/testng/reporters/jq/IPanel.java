package org.testng.reporters.jq;

import org.testng.ISuite;
import org.testng.reporters.XMLStringBuffer;

import java.util.List;

public interface IPanel {
  void generate(List<ISuite> suites, XMLStringBuffer xsb);
}
