package test.dataprovider;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

public class MethodSample {

  @DataProvider(name = "dp1")
  public Object[][] createData(Method m) {
    Assert.assertEquals(m.getName(), "test1");
    Assert.assertEquals(m.getDeclaringClass(), MethodSample.class);

    return new Object[][] {{"Cedric"}, {"Alois"}};
  }

  @Test(dataProvider = "dp1")
  public void test1(String s) {}

  public static int m_test2 = 0;
  public static int m_test3 = 0;

  @DataProvider(name = "dp2")
  public Object[][] createData2(Method m) {
    switch (m.getName()) {
      case "test2":
        m_test2++;
        break;
      case "test3":
        m_test3++;
        break;
      default:
        throw new RuntimeException("Received method " + m + ", expected test2 or test3");
    }
    Assert.assertEquals(m.getDeclaringClass(), MethodSample.class);

    return new Object[][] {{"Cedric"}};
  }

  @Test(dataProvider = "dp2")
  public void test2(String s) {}

  @Test(dataProvider = "dp2")
  public void test3(String s) {}
}
