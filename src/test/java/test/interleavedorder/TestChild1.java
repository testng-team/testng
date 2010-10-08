package test.interleavedorder;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;


public class TestChild1 extends BaseTestClass {
  @BeforeClass
  public void beforeTestChildOneClass() {
    ppp("beforeTestChild1Class");
    InterleavedInvocationTest.LOG.add("beforeTestChild1Class");
  }

  @AfterClass
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
