package test.configuration;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;
import test.configuration.issue2729.BeforeConfigSampleListener;
import test.configuration.issue2729.BeforeConfigTestSample;

public class ConfigurationListenerTest extends SimpleBaseTest {

  @Test
  public void listenerShouldBeCalled() {
    TestNG tng = create(ConfigurationListenerSampleTest.class);
    Assert.assertFalse(ConfigurationListenerSampleTest.m_passed);
    tng.run();
    Assert.assertTrue(ConfigurationListenerSampleTest.m_passed);
  }

  @Test(description = "github 2729")
  public void beforeConfigShouldExecutedForSkippedConfig() {
    TestNG tng = create(BeforeConfigTestSample.class);
    tng.run();
    Assert.assertEquals(BeforeConfigSampleListener.count, 2);
  }
}
