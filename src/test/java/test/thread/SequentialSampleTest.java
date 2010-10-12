package test.thread;

import org.testng.annotations.Test;

@Test(sequential = true)
public class SequentialSampleTest extends BaseSequentialSample {

  public void f1() {
    addId("SequentialSampleTest.f1()", Thread.currentThread().getId());
  }

  public void f2() {
    addId("SequentialSampleTest.f2()", Thread.currentThread().getId());
  }

  public void f3() {
    addId("SequentialSampleTest.f3()", Thread.currentThread().getId());
  }

}
