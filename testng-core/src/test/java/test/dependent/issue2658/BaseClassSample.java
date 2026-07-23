package test.dependent.issue2658;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class BaseClassSample {
  @Test
  public void test() {
    assertThat(getClass()).isEqualTo(PassingClassSample.class);
  }
}
