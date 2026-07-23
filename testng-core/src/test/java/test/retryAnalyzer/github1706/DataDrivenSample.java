package test.retryAnalyzer.github1706;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DataDrivenSample {
  private AtomicInteger counter = new AtomicInteger(0);

  @Test(retryAnalyzer = LocalRetry.class, dataProvider = "getdata")
  public void testMethod(int i) {
    assertThat(i > 0).isTrue();
    if (counter.incrementAndGet() != 3) {
      fail();
    }
    counter = new AtomicInteger(0);
  }

  @DataProvider(name = "getdata")
  public Object[][] getData() {
    return new Object[][] {{1}, {2}};
  }
}
