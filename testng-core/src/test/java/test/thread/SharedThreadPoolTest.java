package test.thread;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import org.testng.TestNG;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;
import test.thread.issue2019.TestClassSample;

public class SharedThreadPoolTest extends SimpleBaseTest {

  @AfterMethod
  public void cleanup() {
    TestClassSample.threads.clear();
  }

  @Test(description = "GITHUB-2019")
  public void ensureCommonThreadPoolIsUsed() {
    List<Long> list = runSimpleTest(true);
    assertThat(list)
        .withFailMessage(
            "Expecting the regular and data driven tests to run at " + "max in 3 threads")
        .hasSizeLessThanOrEqualTo(3);
  }

  @Test(description = "GITHUB-2019")
  public void ensureCommonThreadPoolIsNotUsed() {
    List<Long> list = runSimpleTest(false);
    assertThat(list)
        .withFailMessage("Expecting the regular and data driven tests to run in at-least 3 threads")
        .hasSizeGreaterThanOrEqualTo(3);
  }

  private static List<Long> runSimpleTest(boolean useSharedGlobalThreadPool) {
    TestNG testng = create(TestClassSample.class);
    testng.shouldUseGlobalThreadPool(useSharedGlobalThreadPool);
    testng.setThreadCount(3);
    testng.setParallel(XmlSuite.ParallelMode.METHODS);
    testng.run();
    Queue<Long> threads = TestClassSample.threads;
    assertThat(threads).hasSize(10);
    return threads.stream().distinct().collect(Collectors.toList());
  }

  @Test(description = "GITHUB-2019")
  public void ensureCommonThreadPoolIsUsedWhenUsedWithSuiteFiles() {
    String suite = "src/test/resources/2019_global_thread_pool_enabled.xml";
    List<Long> list = runSimpleTest(suite);
    assertThat(list)
        .withFailMessage(
            "Expecting the regular and data driven tests to run at " + "max in 3 threads")
        .hasSizeBetween(1, 3);
  }

  @Test(description = "GITHUB-2019")
  public void ensureCommonThreadPoolIsNotUsedWhenUsedWithSuiteFiles() {
    String suite = "src/test/resources/2019_global_thread_pool_disabled.xml";
    List<Long> list = runSimpleTest(suite);
    assertThat(list)
        .withFailMessage(
            "Expecting the regular and data driven tests to run at " + "max in 2 threads")
        .hasSizeBetween(4, 10);
  }

  private static List<Long> runSimpleTest(String suite) {
    TestNG testng = create();
    testng.setTestSuites(Collections.singletonList(suite));
    testng.run();
    Queue<Long> threads = TestClassSample.threads;
    assertThat(threads).hasSize(10);
    return threads.stream().distinct().collect(Collectors.toList());
  }
}
