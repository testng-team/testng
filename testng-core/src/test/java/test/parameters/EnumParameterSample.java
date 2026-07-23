package test.parameters;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class EnumParameterSample {

  public enum MyEnum {
    VALUE_1,
    VALUE_2
  }

  @Test
  @Parameters("parameter")
  public void testMethod(MyEnum parameter) {
    assertThat(parameter).isNotNull();
  }
}
