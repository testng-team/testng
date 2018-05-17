package test.thread.parallelization.issue1773;

import org.testng.TestNG;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.internal.RuntimeBehavior;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class IssueTest extends SimpleBaseTest {
  @BeforeClass
  public void setup() {
    System.setProperty(RuntimeBehavior.TESTNG_THREAD_AFFINITY, "true");
  }

  @Test(dataProvider = "dp1")
  public void testThreadAffinity(Class<?>... classes) {
    XmlSuite xmlsuite = createXmlSuite("test_suite");
    xmlsuite.setParallel(XmlSuite.ParallelMode.CLASSES);
    xmlsuite.setThreadCount(6);
    createXmlTest(xmlsuite, "Test 1", classes);
    TestNG testng = create(xmlsuite);
    testng.setVerbose(2);
    LogGatheringListener listener = new LogGatheringListener();
    testng.addListener(listener);
    testng.run();
    assertThat(listener.log).hasSize(2);
  }

  @Test(dataProvider = "dp2")
  public void testThreadAffinityAcrossTests(XmlSuite.ParallelMode mode, int size) {
    XmlSuite xmlsuite = createXmlSuite("test_suite");
    xmlsuite.setParallel(XmlSuite.ParallelMode.TESTS);
    xmlsuite.setThreadCount(6);
    createXmlTest(xmlsuite, "Test_1", PriorityTestSample1.class,PriorityTestSample2.class).setParallel(mode);
    createXmlTest(xmlsuite, "Test_2", PriorityTestSample1.class,PriorityTestSample2.class).setParallel(mode);
    TestNG testng = create(xmlsuite);
    testng.setVerbose(2);
    LogGatheringListener listener = new LogGatheringListener();
    testng.addListener(listener);
    testng.run();
    assertThat(listener.log).hasSize(size);
  }

  @DataProvider(name = "dp2")
  public Object[][] createTestData() {
    return new Object[][] {
            {XmlSuite.ParallelMode.NONE, 2},
            {XmlSuite.ParallelMode.CLASSES, 4}
    };
  }

  @DataProvider(name = "dp1")
  public Object[][] getData() {
    return new Object[][] {
      {PriorityTestSample1.class, PriorityTestSample2.class},
      {MethodDependenciesSample1.class, MethodDependenciesSample2.class}
    };
  }

  @AfterClass(alwaysRun = true)
  public void teardown() {
    System.setProperty(RuntimeBehavior.TESTNG_THREAD_AFFINITY, "false");
  }
}
