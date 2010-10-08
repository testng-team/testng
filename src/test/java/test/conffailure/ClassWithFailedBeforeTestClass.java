package test.conffailure;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ClassWithFailedBeforeTestClass {
  @BeforeClass
  public void setUpShouldFail() {
    throw new RuntimeException("Failing in setUp");
  }

  // Adding this method or @Configuration will never be invoked
  @Test
  public void dummy() {

  }

}
