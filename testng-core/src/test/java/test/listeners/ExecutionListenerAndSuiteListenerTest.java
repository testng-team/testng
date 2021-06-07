package test.listeners;

import static org.testng.Assert.assertEquals;

import java.util.List;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

public class ExecutionListenerAndSuiteListenerTest extends SimpleBaseTest {

  @Test
  public void executionListenerAndSuiteListenerTest() {
    String suiteFile = getPathToResource("executionlistenersingletoncheck/parent.xml");
    List<XmlSuite> suites = getSuites(suiteFile);
    TestNG testng = new TestNG();
    testng.setXmlSuites(suites);
    testng.run();
    assertEquals(ExecutionListenerAndSuiteListener.getTmpString(), "INITIALIZED");
  }
}
