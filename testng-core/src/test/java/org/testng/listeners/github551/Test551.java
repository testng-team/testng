package org.testng.listeners.github551;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.listeners.samples.github551.ConfigListener;
import org.testng.listeners.samples.github551.TestWithFailingConfig;
import test.SimpleBaseTest;

public class Test551 extends SimpleBaseTest {

  @Test(description = "GITHUB-551")
  public void testExecutionTimeOfFailedConfig() {
    ConfigListener listener = new ConfigListener();

    TestNG testNG = create(TestWithFailingConfig.class);
    testNG.addListener(listener);
    testNG.run();
    assertThat(ConfigListener.executionTime >= TestWithFailingConfig.EXEC_TIME).isTrue();
  }
}
