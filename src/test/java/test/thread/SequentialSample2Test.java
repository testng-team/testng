package test.thread;

import org.testng.annotations.Test;

@Test(sequential = true)
public class SequentialSample2Test extends BaseSequentialSample {

  public void f1() {
    addId("SequentialSample2Test.f1()", Thread.currentThread().getId());
  }

  public void f2() {
    addId("SequentialSample2Test.f2()", Thread.currentThread().getId());
  }

  public void f3() {
    addId("SequentialSample2Test.f3()", Thread.currentThread().getId());
  }

}
