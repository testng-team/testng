package test.dependent.issue2658;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class BaseClassSample {
  @Test
  public void test() {
    assertEquals(getClass(), PassingClassSample.class);
  }
}
