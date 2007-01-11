package test.thread;

import org.testng.annotations.Test;

public class Test2Test extends BaseSequentialSample {

  @Test
  public void f21() { 
    ppp("f21");
    addId(Thread.currentThread().getId());
  }
  
  @Test
  public void f22() { 
    ppp("f22");
    addId(Thread.currentThread().getId());
  }


}
