package test.reports;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

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
    // Since we have another reporting listener that is injected via the service loader file
    // reporting listeners size will now have to be two
    assertThat(tng.getReporters()).hasSize(2);
    assertThat(ReporterListenerForIssue1227.counter).isOne();
  }

  public static class ReporterListenerForIssue1227 implements IReporter {
    static int counter = 0;

    @Override
    public void generateReport(
        List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
      counter++;
    }
  }
}
