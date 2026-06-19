package test.factory;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class OddSample {

  private final int n;

  public OddSample(int n) {
    this.n = n;
  }

  @Test
  public void verify() {
    assertThat(n % 2).isNotZero();
  }
}
