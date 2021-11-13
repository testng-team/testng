package test.configuration;

import java.util.Arrays;
import org.testng.Assert;
import org.testng.annotations.Test;
import test.configuration.sample.ConfigurationTestSample;
import test.configuration.sample.ExternalConfigurationClassSample;
import test.configuration.sample.MethodCallOrderTestSample;
import test.configuration.sample.SuiteTestSample;

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
}
