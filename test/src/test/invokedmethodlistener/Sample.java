package test.invokedmethodlistener;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class Sample {

  @Test
  public void t1() {
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Test
  public void t2() {
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @AfterMethod
  public void am() {}
}
