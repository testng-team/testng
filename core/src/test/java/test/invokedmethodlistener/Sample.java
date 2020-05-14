package test.invokedmethodlistener;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class Sample {

  @Test
  public void t1() {
    try {
      Thread.sleep(100);
    } catch (InterruptedException handled) {
      Thread.currentThread().interrupt();
    }
  }

  @Test
  public void t2() {
    try {
      Thread.sleep(100);
    } catch (InterruptedException handled) {
      Thread.currentThread().interrupt();
    }
  }

  @AfterMethod
  public void am() {}

  @BeforeSuite
  public void bs() {}
}
