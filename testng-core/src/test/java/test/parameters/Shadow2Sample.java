package test.parameters;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class Shadow2Sample {

  @Parameters("a")
  @Test
  public void test2(String a) {
    assertThat(a).isEqualTo("Second");
  }
}
