package test.parameters;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class Shadow1Sample {

  @Parameters("a")
  @Test
  public void test1(String a) {
    assertThat(a).isEqualTo("First");
  }
}
