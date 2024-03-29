package test.listeners.github551;

import org.testng.Assert;
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
    Assert.assertTrue(ConfigListener.executionTime >= TestWithFailingConfig.EXEC_TIME);
  }
}
