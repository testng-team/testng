package org.testng.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.configuration.samples.ConfigurationListenerSampleTest;
import org.testng.configuration.samples.issue2729.BeforeConfigSampleListener;
import org.testng.configuration.samples.issue2729.BeforeConfigTestSample;
import test.SimpleBaseTest;

public class ConfigurationListenerTest extends SimpleBaseTest {

  @Test
  public void listenerShouldBeCalled() {
    TestNG tng = create(ConfigurationListenerSampleTest.class);
    assertThat(ConfigurationListenerSampleTest.m_passed).isFalse();
    tng.run();
    assertThat(ConfigurationListenerSampleTest.m_passed).isTrue();
  }

  @Test(description = "GITHUB-2729")
  public void beforeConfigShouldExecutedForSkippedConfig() {
    TestNG tng = create(BeforeConfigTestSample.class);
    tng.run();
    assertThat(BeforeConfigSampleListener.count).isEqualTo(2);
  }
}
