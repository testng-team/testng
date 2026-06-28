package test.listeners.github551;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class Test551 extends SimpleBaseTest {

  @Test
  public void testExecutionTimeOfFailedConfig() {
    ConfigListener listener = new ConfigListener();

    TestNG testNG = create(TestWithFailingConfig.class);
    testNG.addListener(listener);
    testNG.run();
    assertThat(ConfigListener.executionTime >= TestWithFailingConfig.EXEC_TIME).isTrue();
  }
}
