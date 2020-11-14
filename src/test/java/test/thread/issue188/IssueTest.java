package test.thread.issue188;

import java.util.Collections;
import java.util.Set;
import org.assertj.core.api.Assertions;
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
      TestNG testng = new TestNG();
      XmlSuite xmlSuite = new XmlSuite();
      xmlSuite.setParallel(XmlSuite.ParallelMode.METHODS);
      xmlSuite.setVerbose(2);
      xmlSuite.setThreadCount(10);
      xmlSuite.setName("Parallel Issue Suite");
      createXmlTest(xmlSuite, "Test1", Issue188TestSample.class);
      createXmlTest(xmlSuite, "Test2", Issue188TestSample.class);
      createXmlTest(xmlSuite, "Test3", Issue188TestSample.class);
      testng.setXmlSuites(Collections.singletonList(xmlSuite));
      testng.run();
      Set<Long> timestamps = Issue188TestSample.timestamps.keySet();
      if (timestamps.size() == 1) {
      Assertions.assertThat(Issue188TestSample.timestamps.values().iterator().next())
          .withFailMessage("Since all tests were started simultaneously,test method count should have been 6")
          .hasSize(6);
      } else {
        long answer = Issue188TestSample.timestamps.keySet().stream()
            .reduce(0L, (x, y) -> Math.abs(x - y));
        Assertions.assertThat(answer)
            .withFailMessage("test methods should have started within a lag of max 20 ms")
            .isLessThanOrEqualTo(20);
      }
    } finally {
      System.setProperty(RuntimeBehavior.STRICTLY_HONOUR_PARALLEL_MODE, "false");
    }
  }
}
