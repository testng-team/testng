package test.thread.parallelization.issue2110;

import static org.assertj.core.api.Assertions.assertThat;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.ParallelMode;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test
  public void testMethod() {
    System.setProperty("testng.thread.affinity", "true");
    try {
      XmlSuite xmlsuite = createXmlSuite("2110_suite");
      xmlsuite.setParallel(ParallelMode.CLASSES);
      xmlsuite.setThreadCount(50);
      createXmlTest(xmlsuite, "2110_test", TestClass.class);
      TestNG testng = create(xmlsuite);
      testng.setVerbose(2);
      testng.run();
      assertThat(TestClass.threadIds).hasSize(1);
    } finally {
      System.setProperty("testng.thread.affinity", "false");
    }
  }

}
