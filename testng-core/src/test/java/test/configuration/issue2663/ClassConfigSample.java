package test.configuration.issue2663;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** Class level configuration methods of one class, ordered by priority against each other. */
public class ClassConfigSample {

  @BeforeClass(priority = 2)
  public void alphaBeforeClass() {}

  @BeforeClass(priority = 1)
  public void bravoBeforeClass() {}

  @AfterClass(priority = 2)
  public void alphaAfterClass() {}

  @AfterClass(priority = 1)
  public void bravoAfterClass() {}

  @Test
  public void test() {}
}
