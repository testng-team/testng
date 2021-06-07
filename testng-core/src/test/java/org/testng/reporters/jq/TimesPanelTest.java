package org.testng.reporters.jq;

import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.testng.ISuite;
import org.testng.annotations.Test;
import org.testng.internal.paramhandler.FakeSuite;
import org.testng.reporters.XMLStringBuffer;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

public class TimesPanelTest extends SimpleBaseTest {

  private static final String GITHUB_1931 = "GITHUB-1931 [NPE] TimesPanel.maxTime(ISuite suite)";

  @Test(description = GITHUB_1931)
  public void generateReportTimesPanelContentForSuiteWithoutStartedTests() {
    XmlTest xmlTest = createXmlTest("GITHUB_1931", "NPE", Object.class);
    ISuite iSuite = new FakeSuite(xmlTest);
    List<ISuite> suites = new ArrayList<>();
    suites.add(iSuite);
    Model model = new Model(suites);
    TimesPanel panel = new TimesPanel(model);
    XMLStringBuffer buffer = new XMLStringBuffer();
    panel.getContent(iSuite, buffer);
    assertTrue(
        "TimesPanel contains total running time",
        panel.getContent(iSuite, buffer).contains("Total running time: 0 ms"));
  }
}
