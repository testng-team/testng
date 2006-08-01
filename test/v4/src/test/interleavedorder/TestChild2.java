package test.interleavedorder;

import org.testng.annotations.Configuration;


public class TestChild2 extends BaseTestClass {
  @Configuration(beforeTestClass = true)
  public void beforeTestChildTwoClass() {
    ppp("beforeTestChild2Class");
    InterleavedInvocationTest.LOG.add("beforeTestChild2Class");
  }
  
  @Configuration(afterTestClass = true)
  public void afterTestChildTwoClass() {
    ppp("afterTestChild2Class");
    InterleavedInvocationTest.LOG.add("afterTestChild2Class");
  }
  
  private void ppp(String s) {
    if (false) {
      System.out.println("[TestChild2] " + s);
    }
  }

}
