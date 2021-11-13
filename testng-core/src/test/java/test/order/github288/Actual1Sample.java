package test.order.github288;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class Actual1Sample extends BaseSample {

  @DataProvider
  public Object[][] parameters() {
    return new Object[][] {
      new Object[] {"one"}, new Object[] {"two"}, new Object[] {"three"}, new Object[] {"four"}
    };
  }

  @Test
  public void test1() {}

  @Test(dependsOnMethods = "test1")
  public void test3() {}

  @Test(dataProvider = "parameters")
  public void test4(String parameter) {}
}
