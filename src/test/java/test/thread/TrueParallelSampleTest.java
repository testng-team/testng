package test.thread;

import org.testng.annotations.Test;

import java.util.Random;

@Test
public class TrueParallelSampleTest extends BaseThreadTest {
  static Random random = new Random(System.currentTimeMillis());

  private void log(String s) {
    logString(s);
    try {
      Thread.sleep(random.nextInt(10));
    } catch (InterruptedException ex) {
      Thread.yield();
    }
    logString(s);
    logCurrentThread();
  }

  public void m1() {
    log("m1");
  }

  public void m2() {
    log("m2");
  }

  public void m3() {
    log("m3");
  }

  public void m4() {
    log("m4");
  }

  public void m5() {
    log("m5");
  }
}
