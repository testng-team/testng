package test.configuration;

import org.testng.annotations.Test;
import test.configuration.sample.ConfigurationTestSample;
import test.configuration.sample.ExternalConfigurationClass;
import test.configuration.sample.MethodCallOrderTestSample;
import test.configuration.sample.SuiteTestSample;
import test.configuration.sample.VerifySuiteTestSample;

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
    testConfiguration(MethodCallOrderTestSample.class, ExternalConfigurationClass.class);
  }

  @Test
  public void testSuite() {
    testConfiguration(SuiteTestSample.class, VerifySuiteTestSample.class);
  }
}
