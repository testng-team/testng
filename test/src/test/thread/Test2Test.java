package test.thread;

import org.testng.annotations.Test;

public class Test2Test extends BaseSequentialSample {

  @Test
  public void f2() { 
    ppp("f2");
    addId(Thread.currentThread().getId());
  }

}
