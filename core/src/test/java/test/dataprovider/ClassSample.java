package test.dataprovider;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class ClassSample {

  String s;

  @Factory(dataProvider = "dp1")
  public ClassSample(String s) {
    this.s = s;
  }

  @DataProvider(name = "dp1")
  public static Object[][] createData1(Class clazz) {
    Assert.assertEquals(clazz, ClassSample.class);
    return new Object[][] {{"0"}, {"1"}};
  }

  @Test
  public void test1() {}

  @DataProvider(name = "dp2")
  public Object[][] createData2(Class clazz) {
    Assert.assertEquals(clazz, ClassSample.class);
    return new Object[][] {{"Cedric" + s}, {"Alois" + s}};
  }

  @Test(dataProvider = "dp2")
  public void test2(String s) {}
}
