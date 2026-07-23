package test.name;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

@Test(testName = "NAME")
public class NameSample {

  @Test
  public void test() {
    assertThat(true).isTrue();
  }
}
