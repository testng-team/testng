package test.factory.lazy;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

/**
 * Validates lazy instantiation across parallel execution modes: every instance must still be
 * constructed exactly once and every test must run, regardless of how methods are partitioned into
 * workers.
 */
public class LazyFactoryParallelTest extends SimpleBaseTest {

  @DataProvider(name = "modes")
  public Object[][] modes() {
    return new Object[][] {
      {XmlSuite.ParallelMode.METHODS, false},
      {XmlSuite.ParallelMode.INSTANCES, false},
      {XmlSuite.ParallelMode.INSTANCES, true},
      {XmlSuite.ParallelMode.CLASSES, false},
    };
  }

  @Test(dataProvider = "modes")
  public void everyInstanceBuiltOnceAndEveryTestRuns(
      XmlSuite.ParallelMode mode, boolean groupByInstances) {
    CountingFactorySample.reset();

    XmlSuite suite = createXmlSuite("lazy-parallel-" + mode + "-" + groupByInstances);
    suite.setLazyFactory(true);
    suite.setParallel(mode);
    suite.setThreadCount(4);
    suite.setGroupByInstances(groupByInstances);
    XmlTest test = createXmlTest(suite, "t");
    createXmlClass(test, CountingFactorySample.class);

    TestNG tng = create(suite);
    tng.run();

    // Exactly four constructions total means each of the four instances was built once (no instance
    // was constructed twice, none was skipped) ...
    assertThat(CountingFactorySample.CONSTRUCTED)
        .as(
            "each instance constructed exactly once under %s (groupByInstances=%s)",
            mode, groupByInstances)
        .hasValue(4);
    // ... and every instance's test ran.
    assertThat(CountingFactorySample.TESTS_RUN)
        .as("each test runs under %s (groupByInstances=%s)", mode, groupByInstances)
        .containsExactlyInAnyOrder(0, 1, 2, 3);
  }
}
