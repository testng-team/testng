package test.parameters;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class BeforeWithParameterSample {

  @BeforeMethod
  @Parameters("parameter")
  public static void beforeMethod(String parameter) {
    assertThat(parameter).isEqualTo("parameter value");
  }

  @DataProvider(name = "dataProvider")
  public static Object[][] dataProvider() {
    return new Object[][] {{"abc", "def"}};
  }

  @Test(dataProvider = "dataProvider")
  public static void testExample(String a, String b) {
    assertThat(a).isEqualTo("abc");
    assertThat(b).isEqualTo("def");
  }
}
