package test.superclass;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class ChildSampleTest3 extends BaseSampleTest3 {
  @Test
  public void pass() {
    assertThat(true).isTrue();
  }

  @Test
  public void fail() {
    assertThat(false).isTrue();
  }
}
