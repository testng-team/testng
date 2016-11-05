package test.reports;

import org.testng.*;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

import java.util.List;

public class UniqueReporterInjectionTest extends SimpleBaseTest {
    @Test
    public void testPruningOfDuplicateReporter() {
        XmlSuite xmlSuite = createXmlSuite("Suite");
        XmlTest xmlTest = createXmlTest(xmlSuite, "Test");
        createXmlClass(xmlTest, UniqueReporterInjectionSample1.class);
        createXmlClass(xmlTest, UniqueReporterInjectionSample2.class);
        TestNG tng = create(xmlSuite);
        tng.setUseDefaultListeners(false);
        tng.addListener((ITestNGListener) new ReporterListenerForIssue1227());
        tng.run();
        Assert.assertTrue(tng.getReporters().size() == 1);
    }

    public static class ReporterListenerForIssue1227 implements IReporter {
        @Override
        public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {}
    }
}
