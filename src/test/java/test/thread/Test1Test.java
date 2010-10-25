package test.thread;

import org.testng.annotations.Test;

public class Test1Test extends BaseSequentialSample {

  @Test
  public void f11() {
    ppp("f11");
    addId("Test1Test.f11()", Thread.currentThread().getId());
  }

  @Test
  public void f12() {
    ppp("f12");
    addId("Test1Test.f12()", Thread.currentThread().getId());
  }


}
