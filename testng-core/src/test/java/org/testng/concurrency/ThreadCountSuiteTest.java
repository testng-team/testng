package org.testng.concurrency;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import org.testng.ITestNGListener;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

/**
 * Runs the thread-count suite, so the two classes it names are checked at all.
 *
 * <p>{@code TestThreadCountTest} and {@code SuiteThreadCountTest} assert a thread count in
 * {@code @AfterClass}. That count holds only under the {@code thread-count} and {@code parallel}
 * settings of their own suite file. The file sat inside the Java source tree, where nothing loaded
 * it, so neither class had ever run. This test runs that suite.
 *
 * <p>A failed {@code @AfterClass} is a configuration failure, not a failed test. The test methods
 * would still count as passed, so this checks configuration failures and skips as well.
 */
public class ThreadCountSuiteTest extends SimpleBaseTest {

  @Test
  public void theSuiteHonoursTestAndSuiteThreadCounts() {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG tng = create();
    tng.setTestSuites(Collections.singletonList(getPathToResource("concurrency/thread-count.xml")));
    tng.addListener((ITestNGListener) tla);
    tng.run();

    assertThat(tla.getConfigurationFailures()).isEmpty();
    assertThat(tla.getConfigurationSkips()).isEmpty();
    assertThat(tla.getFailedTests()).isEmpty();
    assertThat(tla.getPassedTests()).hasSize(5);
  }
}
