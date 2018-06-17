package test.objectfactory.issue1827;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class SampleTest {
  private int value;

  public SampleTest(int value) {
    this.value = value;
  }

  @Test
  public void test() {
    assertEquals(value, 1);
  }
}
