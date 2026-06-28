package test.thread;

import static org.assertj.core.api.Assertions.assertThat;

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

    assertThat(PriorityInSingleThreadTest.getThreadCount()).isEqualTo(1);
  }
}
