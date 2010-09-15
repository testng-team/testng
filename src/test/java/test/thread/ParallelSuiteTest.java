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
    runTest(2, Arrays.asList(
        getPathToResource("suite-parallel-1.xml"),
        getPathToResource("suite-parallel-2.xml")));
  }

  @Test
  public void suitesShouldRunInParallel2() {
    runTest(3, Arrays.asList(
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
  
  private void runTest(int expected, List<String> paths) {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG tng = create();
    tng.setSuiteThreadPoolSize(5);
//    tng.setVerbose(10);
    tng.setTestSuites(paths);
    tng.addListener(tla);

    BaseThreadTest.initThreadLog();
    tng.run();

    Assert.assertEquals(BaseThreadTest.getThreadCount(), expected);
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
    for (int i = 0 ; i < 2 ; i++) {
      Assert.assertTrue(suitesMap.get(SUITE_NAME_PREFIX + i) 
                   < suitesMap.get(SUITE_NAME_PREFIX + (i + 1)));
    }
  }

  @Test(enabled = false, description = "Number of threads (1) is less than number of levels of suites (2)")
  public void suitesShouldRun() {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG tng = create();
    tng.setTestSuites(Arrays.asList(getPathToResource("suite-parallel-0.xml")));
    tng.setRandomizeSuites(true);
    tng.addListener(tla);
    BaseThreadTest.initThreadLog();
    tng.run();
  }
}
