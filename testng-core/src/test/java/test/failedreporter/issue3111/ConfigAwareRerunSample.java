package test.failedreporter.issue3111;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * Four factory instances with a class-level configuration. The odd ones fail, so a re-run of the
 * generated suite must leave the even ones out entirely -- setup included, not just the test.
 *
 * <p>The {@code @BeforeClass} records itself with row {@code -1} so a single recorder can tell
 * configuration from test execution.
 */
public class ConfigAwareRerunSample {

  private final int instance;

  @Factory(dataProvider = "instances")
  public ConfigAwareRerunSample(int instance) {
    this.instance = instance;
  }

  @DataProvider(name = "instances")
  public static Object[][] instances() {
    return new Object[][] {{0}, {1}, {2}, {3}};
  }

  @BeforeClass
  public void setUp() {
    ExecutedPairs.record(instance, -1);
  }

  @Test
  public void f1() {
    ExecutedPairs.record(instance);
    if (instance % 2 == 1) {
      throw new RuntimeException("instance " + instance);
    }
  }
}
