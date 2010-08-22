package test.thread;

import org.testng.annotations.Test;

public class Sample2 extends BaseThreadTest {

  @Test
  public void s1() {
    logThread(Thread.currentThread().getId());
  }
}
