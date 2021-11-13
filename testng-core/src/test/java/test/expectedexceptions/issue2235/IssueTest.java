package test.expectedexceptions.issue2235;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.ParallelMode;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test(description = "GITHUB-2235")
  public void testExpectedExceptions() {
    XmlSuite xmlSuite = createXmlSuite("main_suite", "main_test", ExampleTestCase.class);
    xmlSuite.setParallel(ParallelMode.METHODS);
    TestNG testng = create(xmlSuite);
    TestListenerAdapter listener = new TestListenerAdapter();
    testng.addListener(listener);
    testng.run();
    assertThat(listener.getPassedTests()).isNotEmpty();
    assertThat(listener.getFailedTests()).isEmpty();
  }
}
