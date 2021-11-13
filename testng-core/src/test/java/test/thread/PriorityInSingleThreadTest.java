package test.thread;

import org.testng.annotations.Test;

@Test(singleThreaded = true)
public class PriorityInSingleThreadTest extends BaseThreadTest {

  @Test(priority = -10)
  public void f1() {
    logCurrentThread();
  }

  @Test(priority = -5)
  public void f2() {
    logCurrentThread();
  }

  @Test(priority = 0)
  public void f3() {
    logCurrentThread();
  }

  @Test(priority = 5)
  public void f4() {
    logCurrentThread();
  }

  @Test(priority = 10)
  public void f5() {
    logCurrentThread();
  }
}
