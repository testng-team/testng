package org.testng.reporters.jq;

import org.testng.ISuite;
import org.testng.reporters.XMLStringBuffer;

public class TimesPanel extends BaseMultiSuitePanel {
  public TimesPanel(Model model) {
    super(model);
  }

  private static String getTag(ISuite suite) {
    return "times-" + suiteToTag(suite);
  }

  @Override
  public String getHeader(ISuite suite) {
    return "Times for " + suite.getName();
  }

  @Override
  public String getPanelName(ISuite suite) {
    return getTag(suite);
  }

  @Override
  public String getContent(ISuite suite, XMLStringBuffer main) {
    return "Times";
  }
}
