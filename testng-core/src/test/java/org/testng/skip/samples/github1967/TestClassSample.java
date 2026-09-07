package test.skip.github1967;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestClassSample {

  @Test
  public void test1min() {
    fail();
  }

  @Test
  public void test2min() {
    fail();
  }

  @BeforeClass
  public void setup() {
    fail();
  }
}
