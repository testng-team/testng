package test.thread;

import org.testng.annotations.Test;

@Test(singleThreaded = true)
public class SingleThreadedSample2Test extends BaseSequentialSample {

  @Test
  public void f1() {
    addId("SingleThreadedSample2Test.f1()", Thread.currentThread().getId());
  }

  @Test
  public void f2() {
    addId("SingleThreadedSample2Test.f2()", Thread.currentThread().getId());
  }

  @Test
  public void f3() {
    addId("SingleThreadedSample2Test.f3()", Thread.currentThread().getId());
  }

}
