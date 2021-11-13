package test.listeners.github551;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestWithFailingConfig {

  public static final int EXEC_TIME = 300;

  @BeforeClass
  public void setUpClassWaitThenFail() throws InterruptedException {
    Thread.sleep(EXEC_TIME);
    throw new RuntimeException();
  }

  @Test
  public void testNothing() {}
}
