package test.issue107;

import org.testng.Assert;
import org.testng.ITestNGListener;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

import java.util.Collections;
import java.util.Map;

public class Issue107Test extends SimpleBaseTest {

  @Test(description = "GITHUB-107, Check that suite parameters set from listener does not affects tests count")
  public void testSuiteParameterModificationFromListener() {
    final XmlSuite suite = createXmlSuite("Simple suite");

    final Map<String, String> parameters = suite.getParameters();
    parameters.put(TestTestngCounter.PARAMETER_NAME, "some value that must be overriden in listener");
    suite.setParameters(parameters);

    runTest(suite);
  }

  @Test(description = "GITHUB-107, Check that suite parameters modification from listener does not affects tests count")
  public void testSuiteParameterSetFromListener() {
    final XmlSuite suite = createXmlSuite("Simple suite");

    runTest(suite);
  }

  private void runTest(XmlSuite suite) {
    final XmlTest test = createXmlTest(suite, "Simple Test", TestTestngCounter.class.getName());
    suite.setTests(Collections.singletonList(test));

    final TestListenerAdapter tla = new TestListenerAdapter();
    final TestNG tng = create();
    tng.setXmlSuites(Collections.singletonList(suite));
    tng.addListener((ITestNGListener) tla);
    tng.run();

    Assert.assertEquals(tla.getFailedTests().size(), 0);
    Assert.assertEquals(tla.getPassedTests().size(), 2);
  }
}
