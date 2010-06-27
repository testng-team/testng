package test.tmp;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

@Listeners(BListener.class)
public class B extends SimpleBaseTest {

  @Test
  public void btest1() {
    try {
      Thread.sleep(12*1000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}

