package test.skip.github1632;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.Test;

public class TestClassSample {

  @Test
  public void passingMethod() {}

  @Test
  @SkipTest
  public void skippingMethod() {}

  @Test
  public void failingMethod() {
    fail();
  }

  @Test
  public void anotherFailingMethod() {
    throw new RuntimeException("Failure");
  }
}
