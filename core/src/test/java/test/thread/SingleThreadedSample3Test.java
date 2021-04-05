package test.thread;

import org.testng.annotations.Test;

@Test(singleThreaded = true)
public class SingleThreadedSample3Test extends BaseSequentialSample {

  @Test
  public void f1() {
    addId("SingleThreadedSample3Test.f1()", Thread.currentThread().getId());
  }

  @Test // (dependsOnMethods = "f1")
  public void f2() {
    addId("SingleThreadedSample3Test.f2()", Thread.currentThread().getId());
  }

  @Test // (dependsOnMethods = "f2")
  public void f3() {
    addId("SingleThreadedSample3Test.f3()", Thread.currentThread().getId());
  }

}
