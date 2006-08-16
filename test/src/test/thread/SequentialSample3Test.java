package test.thread;

import org.testng.annotations.Test;

@Test
public class SequentialSample3Test extends BaseSequentialSample {
  
  @Test
  public void f1() {
    addId(Thread.currentThread().getId());
  }
  
  @Test(dependsOnMethods = "f1")
  public void f2() {
    addId(Thread.currentThread().getId());
  }
  
  @Test(dependsOnMethods = "f2")
  public void f3() {
    addId(Thread.currentThread().getId());
  }  

}
