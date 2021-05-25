package test.dataprovider;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GitHub513Sample {

  @DataProvider
  public static Object[][] testData() {
    return new Object[][] {new Object[] {"a", "b", "c", "d"}};
  }

  @Test(dataProvider = "testData")
  public void test(String fixedArg1, Object fixedArg2, String... args) {
    Assert.assertEquals(fixedArg1, "a");
    Assert.assertEquals(fixedArg2, "b");
    Assert.assertEquals(args.length, 2);
    Assert.assertEquals(args[0], "c");
    Assert.assertEquals(args[1], "d");
  }
}
