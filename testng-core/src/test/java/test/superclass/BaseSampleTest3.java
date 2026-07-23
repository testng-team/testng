package test.superclass;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class BaseSampleTest3 {
  @Test
  public void base() {
    assertThat(true).isTrue();
  }
}
