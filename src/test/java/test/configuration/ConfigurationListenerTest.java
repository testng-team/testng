package test.configuration;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

public class ConfigurationListenerTest extends SimpleBaseTest {

  @Test
  public void listenerShouldBeCalled() {
    TestNG tng = create(ConfigurationListenerSampleTest.class);
    Assert.assertFalse(ConfigurationListenerSampleTest.m_passed);
    tng.run();
    Assert.assertTrue(ConfigurationListenerSampleTest.m_passed);
  }
}
