package test.testng106;

import org.testng.annotations.Test;


/**
 * This class/interface
 */
public class Test2 {
  @Test
  public void method2() {
    System.out.println("method2");
    FailingSuiteFixture.s_invocations++;
  }

  @Test
  public void method3() {
    System.out.println("method3");
    FailingSuiteFixture.s_invocations++;
  }
}
