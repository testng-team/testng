package test.dataprovider.issue3565;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.testng.CommandLineArgs;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

/**
 * GITHUB-3565. A run set up through {@code TestNG.configure(Map)}, as Maven Surefire does, reports
 * a data provider failure as a skip unless the map asks for a failure.
 */
public class ConfigureMapTest extends SimpleBaseTest {

  @Test(description = "GITHUB-3565")
  public void configureMapReportsADataProviderFailureAsASkipByDefault() {
    TestListenerAdapter results = new TestListenerAdapter();
    TestNG testng = runThroughConfigureMap(new HashMap<>(), results);
    assertThat(results.getFailedTests()).isEmpty();
    assertThat(results.getSkippedTests()).hasSize(1);
    assertThat(testng.getStatus()).as("exit status").isEqualTo(2);
  }

  @Test(description = "GITHUB-3565")
  public void configureMapReportsADataProviderFailureAsAFailureWhenAsked() {
    Map<String, Object> values = new HashMap<>();
    values.put(CommandLineArgs.PROPAGATE_DATA_PROVIDER_FAILURES_AS_TEST_FAILURE, "true");
    TestListenerAdapter results = new TestListenerAdapter();
    TestNG testng = runThroughConfigureMap(values, results);
    assertThat(results.getFailedTests()).hasSize(1);
    assertThat(results.getSkippedTests()).isEmpty();
    assertThat(testng.getStatus()).as("exit status").isEqualTo(1);
  }

  @SuppressWarnings("deprecation")
  private static TestNG runThroughConfigureMap(
      Map<String, Object> values, TestListenerAdapter results) {
    TestNG testng = create(BrokenDataProviderSample.class);
    testng.configure(values);
    testng.addListener(results);
    testng.run();
    return testng;
  }
}
