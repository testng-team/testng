package test.thread;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ParallelSuiteTest extends SimpleBaseTest {

  @Test
  public void suitesShouldRunInParallel1() {
    runTest(5, 2, 2, null, Arrays.asList(
        getPathToResource("suite-parallel-1.xml"),
        getPathToResource("suite-parallel-2.xml")));
  }

  @Test
  public void suitesShouldRunInParallel2() {
    runTest(5, 3, 3, null, Arrays.asList(
        getPathToResource("suite-parallel-0.xml")));
  }

  @Test(description = "Number of threads (2) is less than number of suites (3)")
  public void suitesShouldRunInParallel3() {
    final int SUITE_THREAD_POOL_SIZE = 2;
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG tng = create();
    tng.setSuiteThreadPoolSize(SUITE_THREAD_POOL_SIZE);
    tng.setTestSuites(Arrays.asList(getPathToResource("suite-parallel-0.xml")));
    tng.addListener(tla);

    BaseThreadTest.initThreadLog();
    tng.run(); //Shouldn't not deadlock
    Assert.assertEquals(BaseThreadTest.getThreadCount(), SUITE_THREAD_POOL_SIZE);
  }

  private void runTest(int suiteThreadPoolSize, int expectedThreadCount,
        int expectedSuiteCount, Boolean randomizeSuites, List<String> paths) {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG tng = create();
    tng.setSuiteThreadPoolSize(suiteThreadPoolSize);
    tng.setTestSuites(paths);
    tng.addListener(tla);
    if (null != randomizeSuites) {
      tng.setRandomizeSuites(randomizeSuites);
    }

    BaseThreadTest.initThreadLog();
    tng.run();

    Assert.assertEquals(BaseThreadTest.getThreadCount(), expectedThreadCount,
        "Thread count expected:" + expectedThreadCount
        + " actual:" + BaseThreadTest.getThreadCount());
    Assert.assertEquals(BaseThreadTest.getSuitesMap().keySet().size(), expectedSuiteCount);
  }

  @Test
  public void suitesShouldRunInParallel4() {
    runTest(10, 5, 5, null, Arrays.asList(
        getPathToResource("parallel-suites/suite-parallel-1.xml"),
        getPathToResource("parallel-suites/suite-parallel-2.xml"),
        getPathToResource("parallel-suites/suite-parallel-2-1.xml"),
        getPathToResource("parallel-suites/suite-parallel-2-2.xml")));
  }

  @Test
  public void suitesShouldRunInParallel5() {
    runTest(5, 5, 7, null, Arrays.asList(
        getPathToResource("parallel-suites/suite-parallel-0.xml")));
  }

  @Test(description = "Number of threads (2) is less than level of suites (3)")
  public void suitesShouldRunInParallel6() {
    runTest(2, 2, 7, null, Arrays.asList(
          getPathToResource("parallel-suites/suite-parallel-0.xml")));
  }

  @Test(description = "If suiteThreadPoolSize and randomizeSuites are not specified" +
  		" suites should run in order specified in XML")
  public void suitesShouldRunInOrder() {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG tng = create();
    tng.setTestSuites(Arrays.asList(getPathToResource("suite-parallel-0.xml")));
    tng.addListener(tla);
    BaseThreadTest.initThreadLog();
    tng.run();

    Map<String, Long> suitesMap = BaseThreadTest.getSuitesMap();
    Assert.assertEquals(BaseThreadTest.getThreadCount(), 1);
    Assert.assertEquals(suitesMap.keySet().size(), 3);

    final String SUITE_NAME_PREFIX = "Suite Parallel ";
    if (suitesMap.get(SUITE_NAME_PREFIX + 1) > suitesMap.get(SUITE_NAME_PREFIX + 2)) {
      Assert.fail("Suite " + (SUITE_NAME_PREFIX + 1) + " should have run before "
          + (SUITE_NAME_PREFIX + 2));
    }
    Assert.assertTrue(suitesMap.get(SUITE_NAME_PREFIX + 2)
          <= suitesMap.get(SUITE_NAME_PREFIX + 0));

  }

  @Test(description = "Number of threads (1) is less than number of levels of suites (2)")
  public void suitesShouldRun1() {
    runTest(1, 1, 3, true, Arrays.asList(
          getPathToResource("suite-parallel-0.xml")));

//    runTest(1, 1, 7, true, Arrays.asList(
//          getPathToResource("parallel-suites/suite-parallel-0.xml")));
//
//    runTest(2, 2, 7, true, Arrays.asList(
//          getPathToResource("parallel-suites/suite-parallel-0.xml")));
  }

}
