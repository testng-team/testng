package test.issue107;

import java.util.Map;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.xml.XmlSuite;

public class MySuiteListener implements ISuiteListener {
  public void onFinish(ISuite suite) {}

  public void onStart(ISuite suite) {
    final XmlSuite xmlSuite = suite.getXmlSuite();
    final Map<String, String> parameters = xmlSuite.getParameters();
    parameters.put(TestTestngCounter.PARAMETER_NAME, TestTestngCounter.EXPECTED_VALUE);
    xmlSuite.setParameters(parameters);
  }
}
