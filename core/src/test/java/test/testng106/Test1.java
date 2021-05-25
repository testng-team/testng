package test.testng106;

import org.testng.annotations.Test;


/**
 * This class/interface
 */
public class Test1 {
  @Test
  public void method1() {
    System.out.println("method1");
    FailingSuiteFixture.s_invocations++;
  }
}
