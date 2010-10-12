package test.interleavedorder;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;


public class TestChild2 extends BaseTestClass {
  @BeforeClass
  public void beforeTestChildTwoClass() {
    ppp("beforeTestChild2Class");
    InterleavedInvocationTest.LOG.add("beforeTestChild2Class");
  }

  @AfterClass
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
