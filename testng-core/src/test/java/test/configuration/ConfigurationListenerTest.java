package test.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;
import test.configuration.issue2729.BeforeConfigSampleListener;
import test.configuration.issue2729.BeforeConfigTestSample;

public class ConfigurationListenerTest extends SimpleBaseTest {

  @Test
  public void listenerShouldBeCalled() {
    TestNG tng = create(ConfigurationListenerSampleTest.class);
    assertThat(ConfigurationListenerSampleTest.m_passed).isFalse();
    tng.run();
    assertThat(ConfigurationListenerSampleTest.m_passed).isTrue();
  }

  @Test(description = "github 2729")
  public void beforeConfigShouldExecutedForSkippedConfig() {
    TestNG tng = create(BeforeConfigTestSample.class);
    tng.run();
    assertThat(BeforeConfigSampleListener.count).isEqualTo(2);
  }
}
