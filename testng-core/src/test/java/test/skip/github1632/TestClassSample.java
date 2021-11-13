package test.skip.github1632;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestClassSample {

  @Test
  public void passingMethod() {}

  @Test
  @SkipTest
  public void skippingMethod() {}

  @Test
  public void failingMethod() {
    Assert.fail();
  }

  @Test
  public void anotherFailingMethod() {
    throw new RuntimeException("Failure");
  }
}
