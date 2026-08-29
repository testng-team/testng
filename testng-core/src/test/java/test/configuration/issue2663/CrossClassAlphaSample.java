package test.configuration.issue2663;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/** Unrelated to {@link CrossClassBravoSample}, and last by priority despite being first by name. */
public class CrossClassAlphaSample {

  @BeforeTest(priority = 2)
  public void alphaBeforeTest() {}

  @Test
  public void alphaTest() {}
}
