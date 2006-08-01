package test.interleavedorder;

import org.testng.annotations.Configuration;


public class TestChild1 extends BaseTestClass {
  @Configuration(beforeTestClass = true)
  public void beforeTestChildOneClass() {
    ppp("beforeTestChild1Class");
    InterleavedInvocationTest.LOG.add("beforeTestChild1Class");
  }
  
  @Configuration(afterTestClass = true)
  public void afterTestChildOneClass() {
    ppp("afterTestChild1Class");
    InterleavedInvocationTest.LOG.add("afterTestChild1Class");
  }
  
  private void ppp(String s) {
    if (false) {
      System.out.println("[TestChild1] " + s);
    }
  }
}
