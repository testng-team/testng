package test.configuration.issue2663;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/** Test level configuration methods of one class, ordered by priority against each other. */
public class TestConfigSample {

  @BeforeTest(priority = 2)
  public void alphaBeforeTest() {}

  @BeforeTest(priority = 1)
  public void bravoBeforeTest() {}

  @AfterTest(priority = 2)
  public void alphaAfterTest() {}

  @AfterTest(priority = 1)
  public void bravoAfterTest() {}

  @Test
  public void test() {}
}
