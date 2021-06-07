package test.timeout.github1493;

import java.util.concurrent.TimeUnit;
import org.testng.annotations.Test;

public class TestClassSample {
  @Test(timeOut = 1000)
  public void testMethod() throws Exception {
    TimeUnit.SECONDS.sleep(2);
  }
}
