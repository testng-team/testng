package test.inheritance.testng739;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class A extends BaseClass {

  @BeforeClass
  public void beforeClassA() {
    Assert.fail();
  }

  @Test
  public void testA() {
  }
}
