package test.guice.issue279;

import com.google.inject.Inject;
import java.util.List;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.annotations.Guice;
import org.testng.xml.XmlSuite;

@Guice(moduleFactory = TestDIFactory.class)
public class DummyReporter implements IReporter {
  @Inject private Greeter greeter;

  private static Greeter instance;

  @Override
  public void generateReport(
      List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
    instance = greeter;
  }

  public static Greeter getInstance() {
    return instance;
  }

  static void clearInstance() {
    instance = null;
  }
}
