package org.testng.reporters.jq;

import org.testng.reporters.XMLStringBuffer;

public class BannerPanel extends BasePanel {

  public BannerPanel(Model model) {
    super(model);
  }

  @Override
  public void generate(XMLStringBuffer xsb) {
    xsb.push(D, C, "top-banner-root");
    xsb.addRequired(S, "Test results", C, "top-banner-title-font");
    xsb.addEmptyElement("br");
    int failedCount = getModel().getAllFailedResults().size();
    String testResult = failedCount > 0 ? ", " + pluralize(failedCount, "failed test") : "";
    String subTitle = pluralize(getModel().getSuites().size(), "suite")
        + testResult;
    xsb.addRequired(S, subTitle, C, "top-banner-font-1");
    xsb.pop(D);
  }

}
