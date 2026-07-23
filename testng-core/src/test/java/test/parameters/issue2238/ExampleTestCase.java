package test.parameters.issue2238;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class ExampleTestCase {

  @Test
  @Parameters("value")
  public void testMethod(int value) {
    int expected = Integer.parseInt(System.getProperty("value"));
    assertThat(value).isEqualTo(expected);
  }
}
