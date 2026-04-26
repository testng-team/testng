package test.thread.issue188;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.internal.RuntimeBehavior;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test
  public void testSuiteLevelParallelMode() {
    System.setProperty(RuntimeBehavior.STRICTLY_HONOUR_PARALLEL_MODE, "true");
    try {
      Issue188TestSample.reset();
      TestNG testng = new TestNG();
      XmlSuite xmlSuite = new XmlSuite();
      xmlSuite.setParallel(XmlSuite.ParallelMode.METHODS);
      xmlSuite.setThreadCount(10);
      xmlSuite.setName("Parallel Issue Suite");
      createXmlTest(xmlSuite, "Test1", Issue188TestSample.class);
      createXmlTest(xmlSuite, "Test2", Issue188TestSample.class);
      createXmlTest(xmlSuite, "Test3", Issue188TestSample.class);
      testng.setXmlSuites(Collections.singletonList(xmlSuite));
      testng.run();
      assertThat(Issue188TestSample.getStartedTestMethods())
          .withFailMessage("All test methods should have started")
          .isEqualTo(Issue188TestSample.EXPECTED_METHOD_INVOCATIONS);
      assertThat(Issue188TestSample.getMaxActiveTestMethods())
          .withFailMessage("All test methods should have run in parallel")
          .isEqualTo(Issue188TestSample.EXPECTED_METHOD_INVOCATIONS);
    } finally {
      System.setProperty(RuntimeBehavior.STRICTLY_HONOUR_PARALLEL_MODE, "false");
    }
  }
}
