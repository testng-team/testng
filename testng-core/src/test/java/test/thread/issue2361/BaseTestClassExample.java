package test.thread.issue2361;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.testng.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.Test;

@Test(singleThreaded = true)
public class BaseTestClassExample {

  private final AtomicInteger currentTests = new AtomicInteger();

  protected void test() {
    int currentTests = this.currentTests.incrementAndGet();
    try {
      assertEquals(currentTests, 1);
      MILLISECONDS.sleep(10);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } finally {
      this.currentTests.decrementAndGet();
    }
  }

  @Test
  public void test1() {
    test();
  }

  @Test
  public void test2() {
    test();
  }

  @Test
  public void test3() {
    test();
  }

  @Test
  public void test4() {
    test();
  }
}
