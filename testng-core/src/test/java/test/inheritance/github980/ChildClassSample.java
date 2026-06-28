package test.inheritance.github980;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class ChildClassSample extends ParentClassSample {
  @Test
  public void c() {
    assertThat(true).isTrue();
  }

  @Test
  public void d() {
    assertThat(true).isTrue();
  }
}
