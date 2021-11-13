package test.thread;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.BaseTest;

public class SingleThreadForParallelMethodsTest extends BaseTest {

  @Test(description = "GITHUB #1066: Regression is in priority. It broke parallel mode")
  public void testPriorityDoesNotAffectSingleThreadOrder() {
    PriorityInSingleThreadTest.initThreadLog();
    Helper.reset();
    addClass(PriorityInSingleThreadTest.class);

    setParallel(XmlSuite.ParallelMode.METHODS);
    setThreadCount(10);

    run();

    Assert.assertEquals(PriorityInSingleThreadTest.getThreadCount(), 1);
  }
}
