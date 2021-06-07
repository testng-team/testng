package test.thread.parallelization;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.TestNG;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.internal.RuntimeBehavior;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.ParallelMode;
import test.SimpleBaseTest;
import test.thread.parallelization.issue1773.LogGatheringListener;
import test.thread.parallelization.issue1773.MethodDependenciesSample1;
import test.thread.parallelization.issue1773.MethodDependenciesSample2;
import test.thread.parallelization.issue1773.PriorityTestSample1;
import test.thread.parallelization.issue1773.PriorityTestSample2;
import test.thread.parallelization.issue2110.TestClass;
import test.thread.parallelization.issue2321.TestMultipleInstance;

public class ThreadAffinityTest extends SimpleBaseTest {
  @BeforeClass
  public void setup() {
    System.setProperty(RuntimeBehavior.TESTNG_THREAD_AFFINITY, "true");
  }

  @BeforeMethod
  public void beforeMethod() {
    LogGatheringListener.reset();
  }

  @Test(dataProvider = "dp1")
  public void testThreadAffinity(Class<?>... classes) {
    XmlSuite xmlsuite = createXmlSuite("test_suite");
    xmlsuite.setParallel(XmlSuite.ParallelMode.CLASSES);
    xmlsuite.setThreadCount(6);
    createXmlTest(xmlsuite, "Test_1", classes);
    TestNG testng = create(xmlsuite);
    testng.run();
    Map<Class<?>, Set<Long>> map = LogGatheringListener.getLog().get("Test_1");
    SoftAssertions softly = new SoftAssertions();
    Arrays.stream(classes)
        .forEach(
            each ->
                softly
                    .assertThat(map.get(each))
                    .withFailMessage(
                        "All tests within "
                            + each.getName()
                            + " should have run in the same thread")
                    .hasSize(1));
    softly.assertAll();
  }

  @Test(dataProvider = "dp2")
  public void testThreadAffinityAcrossTests(XmlSuite.ParallelMode mode) {
    XmlSuite xmlsuite = createXmlSuite("test_suite");
    xmlsuite.setParallel(XmlSuite.ParallelMode.TESTS);
    xmlsuite.setThreadCount(6);
    Class<?>[] testClasses = new Class<?>[] {PriorityTestSample1.class, PriorityTestSample2.class};
    createXmlTest(xmlsuite, "Test_1", testClasses).setParallel(mode);
    createXmlTest(xmlsuite, "Test_2", testClasses).setParallel(mode);
    TestNG testng = create(xmlsuite);
    testng.run();
    SoftAssertions softly = new SoftAssertions();
    for (String test : Arrays.asList("Test_1", "Test_2")) {
      Map<Class<?>, Set<Long>> map = LogGatheringListener.getLog().get(test);
      for (Class<?> cls : testClasses) {
        softly
            .assertThat(map.get(cls))
            .withFailMessage(
                "All tests within " + cls.getName() + " should have run in the same thread")
            .hasSize(1);
      }
    }
    softly.assertAll();
  }

  @DataProvider(name = "dp2")
  public Object[][] createTestData() {
    return new Object[][] {{XmlSuite.ParallelMode.NONE}, {XmlSuite.ParallelMode.CLASSES}};
  }

  @DataProvider(name = "dp1")
  public Object[][] getData() {
    return new Object[][] {
      {PriorityTestSample1.class, PriorityTestSample2.class},
      {MethodDependenciesSample1.class, MethodDependenciesSample2.class}
    };
  }

  @Test(description = "GITHUB-2321")
  public void testThreadAffinityInFactoryInstances() {
    ParallelMode mode = ParallelMode.INSTANCES;
    XmlSuite xmlsuite = createXmlSuite("test_suite");
    xmlsuite.setParallel(mode);
    xmlsuite.setThreadCount(6);
    createXmlTest(xmlsuite, "Test_1", TestMultipleInstance.class).setParallel(mode);
    TestNG testng = create(xmlsuite);
    testng.run();
    Assertions.assertThat(testng.getStatus()).isEqualTo(0);
  }

  @Test(description = "GITHUB-2110")
  public void ensureNoNPEIsThrown() {
    XmlSuite xmlsuite = createXmlSuite("2110_suite");
    xmlsuite.setParallel(ParallelMode.CLASSES);
    xmlsuite.setThreadCount(50);
    createXmlTest(xmlsuite, "2110_test", TestClass.class);
    TestNG testng = create(xmlsuite);
    testng.run();
    assertThat(TestClass.getThreadIds()).hasSize(1);
  }

  @AfterClass(alwaysRun = true)
  public void teardown() {
    System.setProperty(RuntimeBehavior.TESTNG_THREAD_AFFINITY, "false");
  }
}
