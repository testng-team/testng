package test.listeners.github1029;

import org.testng.Assert;
import org.testng.annotations.Test;

public class Issue1029SampleTestClassWithOneMethod {
  @Test(invocationCount = 5, threadPoolSize = 10)
  public void a() {
    Assert.assertTrue(true);
  }
}
