package test.objectfactory.github1827;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class GitHub1827Sample {

  private final int value;

  public GitHub1827Sample(int value) {
    this.value = value;
  }

  @Test
  public void test() {
    assertThat(value).isOne();
  }
}
