package test.objectfactory.issue2676;

import java.util.List;
import org.testng.IAlterSuiteListener;
import org.testng.xml.XmlSuite;

public class LocalSuiteAlteringListener implements IAlterSuiteListener {

  @Override
  public void alter(List<XmlSuite> suites) {
    suites.forEach(each -> each.setObjectFactoryClass(LoggingObjectFactorySample.class));
  }
}
