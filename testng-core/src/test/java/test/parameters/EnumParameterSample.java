package test.parameters;

import org.testng.Assert;
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
    Assert.assertNotNull(parameter);
  }
}
