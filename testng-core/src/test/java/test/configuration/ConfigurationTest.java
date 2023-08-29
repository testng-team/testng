package test.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.configuration.issue2726.TestClassSample;
import test.configuration.issue2743.SuiteRunnerIssueTestSample;
import test.configuration.sample.ConfigurationTestSample;
import test.configuration.sample.ExternalConfigurationClassSample;
import test.configuration.sample.MethodCallOrderTestSample;
import test.configuration.sample.SuiteTestSample;
import test.listeners.issue2961.OnlyOnceConfigurationThatFailsTestSample;
import test.listeners.issue2961.OnlyOnceConfigurationThatPassesTestSample;

/**
 * Test @Configuration
 *
 * @author cbeust
 */
public class ConfigurationTest extends ConfigurationBaseTest {
  @Test
  public void testConfiguration() {
    testConfiguration(ConfigurationTestSample.class);
  }

  @Test
  public void testMethodCallOrder() {
    testConfiguration(MethodCallOrderTestSample.class, ExternalConfigurationClassSample.class);
  }

  @Test
  public void testSuite() {
    testConfiguration(SuiteTestSample.class);
    Assert.assertEquals(Arrays.asList(1, 2, 3, 4, 5), SuiteTestSample.m_order);
  }

  @Test
  public void testSuiteRunnerWithDefaultConfiguration() {
    TestNG testNG = create(SuiteRunnerIssueTestSample.class);
    testNG.run();

    Assert.assertEquals(testNG.getStatus(), 0);
  }

  @Test(description = "GITHUB-2726")
  public void testAfterClassCalledOnlyOnceForParallelTestMethods() {
    TestNG testng = create(TestClassSample.class);
    testng.setParallel(XmlSuite.ParallelMode.METHODS);
    testng.setVerbose(2);
    testng.run();
    assertThat(TestClassSample.beforeLogs).hasSize(1);
    assertThat(TestClassSample.afterLogs).hasSize(1);
  }

  @Test(description = "GITHUB-2961", dataProvider = "produceTestClasses")
  public void ensureFirstTimeOnlyConfigsHaveProperTestStatuses(Class<?> clazz) {
    TestNG testng = create(clazz);
    testng.setVerbose(2);
    testng.run();
    assertThat(testng.getStatus()).isZero();
  }

  @DataProvider(name = "produceTestClasses")
  public Object[][] produceTestClasses() {
    return new Object[][] {
      {OnlyOnceConfigurationThatFailsTestSample.class},
      {OnlyOnceConfigurationThatPassesTestSample.class}
    };
  }
}
