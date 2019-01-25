package test.listeners.issue1952;

import java.util.concurrent.TimeUnit;
import org.testng.annotations.Test;

public class TestclassSample {

  @Test(timeOut = 500)
  public void testMethod() throws InterruptedException {
    TimeUnit.SECONDS.sleep(10);
  }
}
