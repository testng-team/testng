package test.guice.issue279;

import com.google.inject.Inject;
import java.util.List;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.annotations.Guice;
import org.testng.xml.XmlSuite;

@Guice
public class DummyReporterWithoutModuleFactory implements IReporter {
  @Inject private Vehicle vehicle;

  private static Vehicle instance;

  @Override
  public void generateReport(
      List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
    instance = vehicle;
  }

  public static Vehicle getInstance() {
    return instance;
  }

  static void clearInstance() {
    instance = null;
  }
}
