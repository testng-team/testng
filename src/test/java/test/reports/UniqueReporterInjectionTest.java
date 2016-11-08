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
        Assert.assertEquals(tng.getReporters().size(),1);
        Assert.assertEquals(ReporterListenerForIssue1227.counter, 1);
    }

    public static class ReporterListenerForIssue1227 implements IReporter {
        static int counter = 0;
        @Override
        public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
            counter++;
        }
    }
}
