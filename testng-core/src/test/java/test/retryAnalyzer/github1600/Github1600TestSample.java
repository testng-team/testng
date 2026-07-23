package test.retryAnalyzer.github1600;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class Github1600TestSample {
  private static int a = 2;

  @Test
  public void test1() {
    assertThat(a).isEqualTo(2);
    a++;
  }
}
