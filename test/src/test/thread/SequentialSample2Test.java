package test.thread;

import org.testng.annotations.Test;

@Test(sequential = true)
public class SequentialSample2Test extends BaseSequentialSample {
  
  @Test
  public void f1() {
    addId(Thread.currentThread().getId());
  }
  
  @Test
  public void f2() {
    addId(Thread.currentThread().getId());
  }
  
  @Test
  public void f3() {
    addId(Thread.currentThread().getId());
  }  

}
