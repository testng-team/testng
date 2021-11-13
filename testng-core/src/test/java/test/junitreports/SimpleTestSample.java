package test.junitreports;

import org.testng.annotations.Test;

public class SimpleTestSample {

  @Test(enabled = false)
  public void iShouldNeverBeExecuted() {}

  @Test
  public void masterTest() {
    throw new IllegalStateException("Simulating a llegal state.");
  }

  @Test(dependsOnMethods = "masterTest")
  public void childTest() {}
}
