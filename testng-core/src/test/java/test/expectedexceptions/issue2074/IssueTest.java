package test.expectedexceptions.issue2074;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.ParallelMode;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test(description = "GITHUB-2074")
  public void testIfInterruptsAreCleared() {
    XmlSuite xmlSuite = createXmlSuite("my_suite", "my_test", FailingInterruptTest.class);
    xmlSuite.setParallel(ParallelMode.METHODS);
    TestNG tng = create(xmlSuite);
    TestListenerAdapter listener = new TestListenerAdapter();
    tng.addListener(listener);
    tng.run();
    assertThat(listener.getFailedTests()).isEmpty();
    assertThat(listener.getConfigurationFailures()).isEmpty();
  }
}
