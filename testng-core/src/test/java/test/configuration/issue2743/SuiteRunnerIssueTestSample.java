package test.configuration.issue2743;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import org.testng.IClassListener;
import org.testng.IInvokedMethodListener;
import org.testng.ISuite;
import org.testng.ITestNGMethod;
import org.testng.ITestRunnerFactory;
import org.testng.SuiteRunner;
import org.testng.TestRunner;
import org.testng.annotations.Test;
import org.testng.internal.Configuration;
import org.testng.internal.IConfiguration;
import org.testng.reporters.JUnitXMLReporter;
import org.testng.reporters.TestHTMLReporter;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

public class SuiteRunnerIssueTestSample {

  @Test
  public void suiteRunnerSample() {
    XmlSuite suite = new XmlSuite();
    suite.setName("TestSuite");
    XmlTest test = new XmlTest(suite);
    test.setName("ChildTests");
    List<XmlClass> classes = new ArrayList<>();
    classes.add(new XmlClass("test.configuration.issue2743.SuiteRunnerIssueTestSample"));
    test.setXmlClasses(classes);

    final IConfiguration configuration = new Configuration();
    final boolean useDefaultListeners = true;

    LocalFactory factory = new LocalFactory(configuration, useDefaultListeners);
    SuiteRunner suiteRunner =
        new SuiteRunner(
            configuration,
            suite,
            "outputDir",
            factory,
            useDefaultListeners,
            Comparator.comparingInt(ITestNGMethod::getPriority));
    factory.suiteRunner = suiteRunner;
    suiteRunner.run();
  }

  private static class LocalFactory implements ITestRunnerFactory {
    private final IConfiguration configuration;
    private final boolean useDefaultListeners;
    private SuiteRunner suiteRunner;

    private LocalFactory(IConfiguration configuration, boolean useDefaultListeners) {
      this.configuration = configuration;
      this.useDefaultListeners = useDefaultListeners;
    }

    @Override
    public TestRunner newTestRunner(
        ISuite suite,
        XmlTest xmlTest,
        Collection<IInvokedMethodListener> listeners,
        List<IClassListener> classListeners) {
      TestRunner runner =
          new TestRunner(
              configuration,
              suite,
              xmlTest,
              false /* skipFailedInvocationCounts */,
              listeners,
              classListeners,
              suiteRunner);
      if (useDefaultListeners) {
        runner.addListener(new TestHTMLReporter());
        runner.addListener(new JUnitXMLReporter());
      }

      return runner;
    }
  }
}
