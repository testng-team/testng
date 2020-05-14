package test.dataprovider;

import org.testng.Assert;
import org.testng.ITestNGMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ITestNGMethodSample {

  @DataProvider(name = "dp1")
  public Object[][] createData(ITestNGMethod m) {
    Assert.assertEquals(m.getMethodName(), "test1");
    Assert.assertEquals(m.getConstructorOrMethod().getMethod().getName(), "test1");
    Assert.assertEquals(m.getRealClass(), ITestNGMethodSample.class);

    return new Object[][] {{"Cedric"}, {"Alois"}};
  }

  @Test(dataProvider = "dp1")
  public void test1(String s) {}
}
