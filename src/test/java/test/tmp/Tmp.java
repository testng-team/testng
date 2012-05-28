package test.tmp;

import org.testng.annotations.Test;

import java.util.Random;

public class Tmp {

  @Test(invocationCount = 10, threadPoolSize = 5)
  public void f() {
    ppp("START " + Thread.currentThread().getId());
    try {
      Random r = new Random();
      Thread.sleep(Math.abs(r.nextInt() % 300));
    }
    catch (InterruptedException handled) {
      Thread.currentThread().interrupt();
      handled.printStackTrace();
    }
    ppp("END " + Thread.currentThread().getId());
  }

  private void ppp(String string) {
    System.out.println("[Tmp] " + string);
  }

}
