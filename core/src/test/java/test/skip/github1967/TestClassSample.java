package test.skip.github1967;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestClassSample {

  @Test
  public void test1min() {
    Assert.fail();
  }

  @Test
  public void test2min() {
    Assert.fail();
  }

  @BeforeClass
  public void setup() {
    Assert.fail();
  }
}
