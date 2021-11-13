package test.parameters;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class BeforeWithParameterSample {

  @BeforeMethod
  @Parameters("parameter")
  public static void beforeMethod(String parameter) {
    Assert.assertEquals(parameter, "parameter value");
  }

  @DataProvider(name = "dataProvider")
  public static Object[][] dataProvider() {
    return new Object[][] {{"abc", "def"}};
  }

  @Test(dataProvider = "dataProvider")
  public static void testExample(String a, String b) {
    Assert.assertEquals(a, "abc");
    Assert.assertEquals(b, "def");
  }
}
