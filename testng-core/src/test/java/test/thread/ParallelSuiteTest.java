package test.thread;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.testng.ITestNGListener;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

public class ParallelSuiteTest extends SimpleBaseTest {

  @Test
  public void suitesShouldRunInParallel1() {
    runTest(
        5,
        2,
        2,
        null,
        Arrays.asList(
            getPathToParallelResource("simple-suite-parallel-1.xml"),
            getPathToParallelResource("simple-suite-parallel-2.xml")));
  }

  @Test
  public void suitesShouldRunInParallel2() {
    runTest(
        5,
        3,
        3,
        null,
        Collections.singletonList(getPathToParallelResource("simple-suite-parallel-0.xml")));
  }

  @Test(description = "Number of threads (2) is less than number of suites (3)")
  public void suitesShouldRunInParallel3() {
    final int SUITE_THREAD_POOL_SIZE = 2;
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG tng = create();
    tng.setSuiteThreadPoolSize(SUITE_THREAD_POOL_SIZE);
    tng.setTestSuites(
        Collections.singletonList(getPathToParallelResource("simple-suite-parallel-0.xml")));
    tng.addListener((ITestNGListener) tla);

    BaseThreadTest.initThreadLog();
    tng.run(); // Shouldn't not deadlock
    assertThat(BaseThreadTest.getThreadCount()).isEqualTo(SUITE_THREAD_POOL_SIZE);
  }

  @Test
  public void suitesShouldRunInParallel4() {
    final int TOTAL_SUITE_COUNT_INCLUDING_DUPLICATES = 8;
    runTest(
        10,
        TOTAL_SUITE_COUNT_INCLUDING_DUPLICATES,
        TOTAL_SUITE_COUNT_INCLUDING_DUPLICATES,
        null,
        Arrays.asList(
            getPathToParallelResource("suite-parallel-1.xml"),
            getPathToParallelResource("suite-parallel-2.xml"),
            getPathToParallelResource("suite-parallel-2-1.xml"),
            getPathToParallelResource("suite-parallel-2-2.xml")));
  }

  @Test
  public void suitesShouldRunInParallel5() {
    runTest(
        5,
        5,
        7,
        null,
        Collections.singletonList(getPathToParallelResource("suite-parallel-0.xml")));
  }

  @Test(description = "Number of threads (2) is less than level of suites (3)")
  public void suitesShouldRunInParallel6() {
    runTest(
        2,
        2,
        7,
        null,
        Collections.singletonList(getPathToParallelResource("suite-parallel-0.xml")));
  }

  @Test(
      description =
          "If suiteThreadPoolSize and randomizeSuites are not specified"
              + " suites should run in order specified in XML")
  public void suitesShouldRunInOrder() {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG tng = create();
    tng.setTestSuites(
        Collections.singletonList(getPathToParallelResource("simple-suite-parallel-0.xml")));
    tng.addListener((ITestNGListener) tla);
    BaseThreadTest.initThreadLog();
    tng.run();

    Map<String, Long> suitesMap = BaseThreadTest.getSuitesMap();
    assertThat(BaseThreadTest.getThreadCount()).isEqualTo(1);
    assertThat(suitesMap.keySet().size()).isEqualTo(3);

    final String SUITE_NAME_PREFIX = "Suite Parallel ";
    if (suitesMap.get(SUITE_NAME_PREFIX + 1) > suitesMap.get(SUITE_NAME_PREFIX + 2)) {
      fail(
          "Suite "
              + (SUITE_NAME_PREFIX + 1)
              + " should have run before "
              + (SUITE_NAME_PREFIX + 2));
    }
    assertThat(suitesMap.get(SUITE_NAME_PREFIX + 2) <= suitesMap.get(SUITE_NAME_PREFIX + 0))
        .isTrue();
  }

  @Test(description = "Number of threads (1) is less than number of levels of suites (2)")
  public void suitesShouldRun1() {
    runTest(
        1,
        1,
        3,
        true,
        Collections.singletonList(getPathToParallelResource("simple-suite-parallel-0.xml")));
  }

  @Test(description = "Child suite should obey threadCount parameter")
  public void childSuiteObeyParentThreadCount() {
    /* parent suite has no tests, so only child suite counts */
    final int EXPECTED_SUITE_COUNT = 1;
    runTest(
        1,
        2,
        XmlSuite.ParallelMode.CLASSES,
        2,
        EXPECTED_SUITE_COUNT,
        null,
        List.of(getPathToParallelResource("inherit-thread-count-parent.yaml")));
  }

  private void runTest(
      int suiteThreadPoolSize,
      int expectedThreadCount,
      int expectedSuiteCount,
      Boolean randomizeSuites,
      List<String> paths) {
    runTest(
        suiteThreadPoolSize,
        null,
        null,
        expectedThreadCount,
        expectedSuiteCount,
        randomizeSuites,
        paths);
  }

  private void runTest(
      int suiteThreadPoolSize,
      Integer threadCount,
      XmlSuite.ParallelMode parallelMode,
      int expectedThreadCount,
      int expectedSuiteCount,
      Boolean randomizeSuites,
      List<String> paths) {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG tng = create();
    tng.setSuiteThreadPoolSize(suiteThreadPoolSize);
    if (threadCount != null) {
      tng.setThreadCount(threadCount);
    }
    if (parallelMode != null) {
      tng.setParallel(parallelMode);
    }
    tng.setTestSuites(paths);
    tng.addListener((ITestNGListener) tla);
    if (null != randomizeSuites) {
      tng.setRandomizeSuites(randomizeSuites);
    }

    BaseThreadTest.initThreadLog();
    tng.run();

    assertThat(BaseThreadTest.getThreadCount())
        .withFailMessage(
            "Thread count expected:"
                + expectedThreadCount
                + " actual:"
                + BaseThreadTest.getThreadCount())
        .isEqualTo(expectedThreadCount);
    assertThat(BaseThreadTest.getSuitesMap().keySet().size())
        .withFailMessage("Suite count is incorrect")
        .isEqualTo(expectedSuiteCount);
  }

  private static String getPathToParallelResource(String resourceName) {
    return getPathToResource(String.format("parallel-suites/%s", resourceName));
  }
}
