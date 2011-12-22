package org.testng.reporters.jq;

import org.testng.reporters.XMLStringBuffer;

public class BannerPanel extends BasePanel {

  public BannerPanel(Model model) {
    super(model);
  }

  @Override
  public void generate(XMLStringBuffer xsb) {
    xsb.push(D, C, "top-banner rounded-window");
    xsb.addRequired(S, "Test results", C, "top-banner-title-font");
    xsb.pop(D);
  }

}
