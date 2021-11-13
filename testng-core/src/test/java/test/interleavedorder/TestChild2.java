package test.interleavedorder;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

public class TestChild2 extends BaseTestClass {
  @BeforeClass
  public void beforeTestChildTwoClass() {
    InterleavedInvocationTest.LOG.add("beforeTestChild2Class");
  }

  @AfterClass
  public void afterTestChildTwoClass() {
    InterleavedInvocationTest.LOG.add("afterTestChild2Class");
  }
}
