package test.retryAnalyzer.github1706;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class ParameterInjectionSample {
  private AtomicInteger counter = new AtomicInteger(0);

  @Test(retryAnalyzer = LocalRetry.class)
  @Parameters({"counter"})
  public void testMethod(int paramCounter) {
    assertThat(paramCounter > 0).isTrue();
    if (counter.incrementAndGet() != 3) {
      fail();
    }
  }
}
