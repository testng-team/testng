package test.listeners.issue2328;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SampleWithConfiguration {

  @BeforeMethod
  public void passingConfig() {}

  @BeforeMethod(dependsOnMethods = "passingConfig")
  public void failingConfig() {
    throw new RuntimeException("Simulate failure");
  }

  @BeforeMethod(dependsOnMethods = "failingConfig")
  public void skippingConfig() {}

  @Test
  public void testMethod() {}
}
