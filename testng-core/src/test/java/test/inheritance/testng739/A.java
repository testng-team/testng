package test.inheritance.testng739;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class A extends BaseClass {

  @BeforeClass
  public void beforeClassA() {
    fail();
  }

  @Test
  public void testA() {}
}
