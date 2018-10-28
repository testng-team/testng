package org.testng.reporters.jq;

import org.testng.ISuite;
import org.testng.annotations.Test;
import org.testng.internal.paramhandler.FakeSuite;
import org.testng.reporters.XMLStringBuffer;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Oleg Shaburov on 28.10.2018
 * shaburov.o.a@gmail.com
 */
public class TimesPanelTest {

    private static final String GITHUB_1931 = "GITHUB-1931 [NPE] TimesPanel.maxTime(ISuite suite)";

    @Test(description = GITHUB_1931)
    public void generateReportTimesPanelContentForSuiteWithoutStartedTests() {
        XmlSuite xmlSuite = new XmlSuite();
        xmlSuite.setName("GITHUB_1931");
        XmlTest xmlTest = new XmlTest(xmlSuite);
        List<XmlClass> xmlClasses = new ArrayList<>();
        xmlClasses.add(new XmlClass(Object.class));
        xmlTest.setXmlClasses(xmlClasses);
        ISuite iSuite = new FakeSuite(xmlTest);
        List<ISuite> suites = new ArrayList<>();
        Model model = new Model(suites);
        XMLStringBuffer buffer = new XMLStringBuffer();

        TimesPanel panel = new TimesPanel(model);
        panel.getContent(iSuite, buffer);
        assertThat(panel.getContent(iSuite, buffer), containsString("Total running time: 0 ms"));
    }

}
