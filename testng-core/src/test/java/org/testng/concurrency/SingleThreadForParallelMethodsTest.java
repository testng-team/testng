package org.testng.concurrency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.concurrency.samples.BaseThreadTest.getThreadCount;

import org.testng.annotations.Test;
import org.testng.concurrency.samples.Helper;
import org.testng.concurrency.samples.PriorityInSingleThreadTest;
import org.testng.xml.XmlSuite;
import test.BaseTest;

public class SingleThreadForParallelMethodsTest extends BaseTest {

  @Test(description = "GITHUB-1066: Regression is in priority. It broke parallel mode")
  public void testPriorityDoesNotAffectSingleThreadOrder() {
    PriorityInSingleThreadTest.initThreadLog();
    Helper.reset();
    addClass(PriorityInSingleThreadTest.class);

    setParallel(XmlSuite.ParallelMode.METHODS);
    setThreadCount(10);

    run();

    assertThat(getThreadCount()).isEqualTo(1);
  }
}
