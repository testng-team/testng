package test.configuration.issue2663;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/** Unrelated to {@link CrossClassAlphaSample}, and first by priority despite being last by name. */
public class CrossClassBravoSample {

  @BeforeTest(priority = 1)
  public void bravoBeforeTest() {}

  @Test
  public void bravoTest() {}
}
