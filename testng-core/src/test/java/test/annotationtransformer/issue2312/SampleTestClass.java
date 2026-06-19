package test.annotationtransformer.issue2312;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class SampleTestClass {

  @Test
  public void testMethod() {
    assertThat(1).isOne();
  }
}
