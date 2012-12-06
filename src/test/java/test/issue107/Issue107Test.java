package test.issue107;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

import java.util.Arrays;
import java.util.Map;

/**
 * @author Vladislav.Rassokhin
 */
public class Issue107Test extends SimpleBaseTest {

  @Test(description = "GITHUB-107, Check that suite parameters set from listener does not affects tests count")
  public void testSuiteParameterModificationFromListener() throws Exception {
    final XmlSuite suite = createXmlSuite("Simple suite");

    final Map<String, String> parameters = suite.getParameters();
    parameters.put(TestTestngCounter.PARAMETER_NAME, "some value that must be overriden in listener");
    suite.setParameters(parameters);

    runTest(suite);
  }

  @Test(description = "GITHUB-107, Check that suite parameters modification from listener does not affects tests count")
  public void testSuiteParameterSetFromListener() throws Exception {
    final XmlSuite suite = createXmlSuite("Simple suite");

    runTest(suite);
  }

  private void runTest(XmlSuite suite) {
    final XmlTest test = createXmlTest(suite, "Simple Test", TestTestngCounter.class.getName());
    suite.setTests(Arrays.asList(test));

    final TestListenerAdapter tla = new TestListenerAdapter();
    final TestNG tng = create();
    tng.setXmlSuites(Arrays.asList(suite));
    tng.addListener(tla);
    tng.run();

    Assert.assertEquals(tla.getFailedTests().size(), 0);
    Assert.assertEquals(tla.getPassedTests().size(), 2);
  }
}
