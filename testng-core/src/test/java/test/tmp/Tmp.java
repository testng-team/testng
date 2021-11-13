package test.tmp;

import java.util.Random;
import org.testng.annotations.Test;
import org.testng.log4testng.Logger;

public class Tmp {

  @Test(invocationCount = 10, threadPoolSize = 5)
  public void f() {
    debug("START " + Thread.currentThread().getId());
    try {
      Random r = new Random();
      Thread.sleep(Math.abs(r.nextInt() % 300));
    } catch (InterruptedException handled) {
      Thread.currentThread().interrupt();
      handled.printStackTrace();
    }
    debug("END " + Thread.currentThread().getId());
  }

  private void debug(String string) {
    Logger.getLogger(getClass()).info("[Tmp] " + string);
  }
}
