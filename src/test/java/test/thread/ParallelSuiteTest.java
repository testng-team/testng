package test.thread;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

import java.util.Arrays;
import java.util.List;

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
}
