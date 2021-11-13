package test.objectfactory.github1827;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class GitHub1827Sample {

  private final int value;

  public GitHub1827Sample(int value) {
    this.value = value;
  }

  @Test
  public void test() {
    assertEquals(value, 1);
  }
}
