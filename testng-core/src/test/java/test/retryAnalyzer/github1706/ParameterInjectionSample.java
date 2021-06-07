package test.retryAnalyzer.github1706;

import java.util.concurrent.atomic.AtomicInteger;
import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class ParameterInjectionSample {
  private AtomicInteger counter = new AtomicInteger(0);

  @Test(retryAnalyzer = LocalRetry.class)
  @Parameters({"counter"})
  public void testMethod(int paramCounter) {
    Assert.assertTrue(paramCounter > 0);
    if (counter.incrementAndGet() != 3) {
      Assert.fail();
    }
  }
}
