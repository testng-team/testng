package test.thread.issue3179;

import java.util.List;
import org.testng.IAlterSuiteListener;
import org.testng.xml.XmlSuite;

public class SampleSuiteAlteringListener implements IAlterSuiteListener {
  @Override
  public void alter(List<XmlSuite> suites) {
    suites.get(0).shouldUseGlobalThreadPool(true);
    suites.get(0).setThreadCount(3);
    suites.get(0).setParallel(XmlSuite.ParallelMode.METHODS);
  }
}
