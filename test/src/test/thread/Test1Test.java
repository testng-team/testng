package test.thread;

import org.testng.annotations.Test;

public class Test1Test extends BaseSequentialSample {

  @Test
  public void f1() { 
    ppp("f1");
    addId(Thread.currentThread().getId());
  }

}
