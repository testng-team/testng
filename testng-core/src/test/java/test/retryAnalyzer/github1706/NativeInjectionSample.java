package test.retryAnalyzer.github1706;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.Test;

public class NativeInjectionSample {
  private AtomicInteger counter = new AtomicInteger(0);

  @Test(retryAnalyzer = LocalRetry.class)
  public void testMethod(Method method) {
    assertThat(method).isNotNull();
    if (counter.incrementAndGet() != 3) {
      fail();
    }
  }
}
