package test.configuration;

import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class BeforeClassWithDisabledTest extends SimpleBaseTest {

  @Test
  public void afterClassShouldRunEvenWithDisabledMethods() {
    TestNG tng = create(ConfigurationDisabledSampleTest.class);
    assertThat(ConfigurationDisabledSampleTest.m_afterWasRun).isFalse();
    tng.run();
    assertThat(ConfigurationDisabledSampleTest.m_afterWasRun).isTrue();
  }
}
