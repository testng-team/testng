package test.parameters;

import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class EnumParameterSample {

  @Test
  @Parameters("parameter")
  public void testMethod(MyEnum parameter) {
    Assert.assertNotNull(parameter);
  }

  public enum MyEnum {
    VALUE_1, VALUE_2
  }
}