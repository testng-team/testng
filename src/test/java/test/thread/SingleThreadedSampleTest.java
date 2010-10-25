package test.thread;

import org.testng.annotations.Test;

@Test(singleThreaded = true)
public class SingleThreadedSampleTest extends BaseSequentialSample {

  @Test
  public void f1() {
    addId("SingleThreadedSampleTest.f1()", Thread.currentThread().getId());
  }

  @Test
  public void f2() {
    addId("SingleThreadedSampleTest.f2()", Thread.currentThread().getId());
  }

  @Test
  public void f3() {
    addId("SingleThreadedSampleTest.f3()", Thread.currentThread().getId());
  }

}
