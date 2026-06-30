package test.methodselectors;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

@Test(description = "GITHUB-1507")
public class ClassWithManyMethodsSample {

  public void testa() {
    assertThat(true).isTrue();
  }

  public void testb() {
    assertThat(true).isTrue();
  }

  public void testc() {
    assertThat(true).isTrue();
  }

  public void testd() {
    assertThat(true).isTrue();
  }
}
