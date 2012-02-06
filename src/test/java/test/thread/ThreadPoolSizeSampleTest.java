package test.thread;

import org.testng.annotations.Test;

public class ThreadPoolSizeSampleTest {
  @Test(threadPoolSize=2, timeOut=100)
  public void willPassBug() throws InterruptedException{
      Thread.sleep(500);
  }

}
