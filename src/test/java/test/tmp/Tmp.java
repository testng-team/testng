package test.tmp;

import java.util.Random;

import org.testng.annotations.Test;

public class Tmp {
  
  @Test(invocationCount = 10, threadPoolSize = 5)
  public void f() {
    ppp("START " + Thread.currentThread().getId());
    try {
      Thread.sleep(Math.abs(new Random().nextInt() % 300));
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
    ppp("END " + Thread.currentThread().getId());
  }

  private void ppp(String string) {
    System.out.println("[Tmp] " + string);
  }

}
