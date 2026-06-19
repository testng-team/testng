package test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

public class ReturnValueTest extends SimpleBaseTest {

  private XmlSuite m_suite;
  private XmlTest m_test;

  @BeforeMethod
  public void before() {
    m_suite = createXmlSuite("suite");
    m_test = createXmlTest(m_suite, "test", ReturnValueSampleTest.class.getName());
  }

  @Test
  public void suiteReturnValueTestShouldBeRun() {
    m_suite.setAllowReturnValues(true);
    runTest(true);
  }

  @Test
  public void suiteReturnValueTestShouldNotBeRun() {
    runTest(false);
  }

  @Test
  public void testReturnValueTestShouldBeRun() {
    m_test.setAllowReturnValues(true);
    runTest(true);
  }

  private void runTest(boolean allowed) {
    TestNG tng = create();
    tng.setXmlSuites(Collections.singletonList(m_suite));
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();

    if (allowed) {
      assertThat(tla.getFailedTests().size()).isEqualTo(0);
      assertThat(tla.getSkippedTests().size()).isEqualTo(0);
      assertTestResultsEqual(tla.getPassedTests(), Collections.singletonList("shouldRun"));
    } else {
      assertThat(tla.getFailedTests().size()).isEqualTo(0);
      assertThat(tla.getPassedTests().size()).isEqualTo(0);
      assertThat(tla.getSkippedTests().size()).isEqualTo(0);
    }
  }
}
