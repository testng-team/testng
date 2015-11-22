package test.thread;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;

public class FactoryTest {

  @Test
  /**
   * In non-parallel mode, we should only have one thread id
   * for the two methods invoked on B.
   */
  public void verifyFactoryNotParallel() {
    runTest(null, 1);
  }

  /**
   * In parallel mode "methods", we should have as many thread id's
   * as there are test methods on B (2).
   */
  @Test
  public void verifyFactoryParallelMethods() {
    runTest(XmlSuite.ParallelMode.METHODS, 2);
  }

  @Test
  public void verifyFactoryParallelTests() {
    runTest(XmlSuite.ParallelMode.TESTS, 1);
  }

  private void runTest(XmlSuite.ParallelMode parallelMode, int expectedThreadIdCount) {
    TestNG tng = new TestNG();
    tng.setVerbose(0);
    tng.setTestClasses(new Class[] { FactorySampleTest.class});
    if (parallelMode != null) {
      tng.setParallel(parallelMode);
    }
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);

    B.setUp();
    tng.run();

    Assert.assertEquals(tla.getPassedTests().size(), 2);
    Assert.assertEquals(B.m_threadIds.size(), expectedThreadIdCount);

//    ppp("# TESTS RUN " + tla.getPassedTests().size()
//        + " ID:" + B.m_threadIds.size());
  }

  private void ppp(String string) {
    System.out.println("[FactoryTest] " + string);
  }



}
