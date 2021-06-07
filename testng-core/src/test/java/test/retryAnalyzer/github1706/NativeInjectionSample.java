package test.retryAnalyzer.github1706;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.Assert;
import org.testng.annotations.Test;

public class NativeInjectionSample {
  private AtomicInteger counter = new AtomicInteger(0);

  @Test(retryAnalyzer = LocalRetry.class)
  public void testMethod(Method method) {
    Assert.assertNotNull(method);
    if (counter.incrementAndGet() != 3) {
      Assert.fail();
    }
  }
}
