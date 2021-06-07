package test.retryAnalyzer;

import java.util.concurrent.atomic.AtomicBoolean;
import org.testng.Assert;
import org.testng.annotations.Test;

public class EventualSuccess {
  private static final AtomicBoolean ranYet = new AtomicBoolean(false);

  @Test(retryAnalyzer = MyRetry.class)
  public void test() {
    if (!ranYet.getAndSet(true)) {
      Assert.fail();
    }
  }
}
