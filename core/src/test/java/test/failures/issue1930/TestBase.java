package test.failures.issue1930;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestBase {

  @Test(dataProvider = "test2")
  public void testNumberEquality(Integer i1, Integer i2) {
    Assert.assertEquals(i1, i2);
  }

  @DataProvider(name = "test2")
  public static Object[][] numbers() {
    return new Object[][] {{2, 2}, {6, 6}, {19, 18}, {22, 21}, {23, 23}};
  }
}
