package test.interleavedorder;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

public class TestChild1 extends BaseTestClass {
  @BeforeClass
  public void beforeTestChildOneClass() {
    InterleavedInvocationTest.LOG.add("beforeTestChild1Class");
  }

  @AfterClass
  public void afterTestChildOneClass() {
    InterleavedInvocationTest.LOG.add("afterTestChild1Class");
  }
}
