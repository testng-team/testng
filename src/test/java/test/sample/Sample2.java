package test.sample;

import org.testng.annotations.Test;

/**
 * This class
 *
 * @author Cedric Beust, Apr 26, 2004
 * 
 */

public class Sample2 {
  
  @Test
  public void method1() {
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
//    System.out.println("@@@@@@@@@@@@@@@@@@@ METHOD1");
  }

  @Test
  public void method2() {
//    System.out.println("@@@@@@@@@@@@@@@@@@@ METHOD2");
  }
  
  @Test
  public void method3() {
//    System.out.println("@@@@@@@@@@@@@@@@@@@ METHOD3");
  }


}
