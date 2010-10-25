package test.dataprovider;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Used by {@link TestNG411Test}. Do not change number of methods or method names
 * @author nullin
 *
 */
public class TestNG411SampleTest
{
  private static final String CHECK_MAX_DATA = "checkMaxData";
  private static final String CHECK_MIN_DATA = "checkMinData";

  @DataProvider(name = CHECK_MAX_DATA)
  public Object[][] dataProviderCheckMax() {
    return new Object[][] {
      { 1, 2, 3, 3 },
    };
  }

  @Test(description = "Number of parameters to this test don't match the " +
      "ones passed by data provider", dataProvider = CHECK_MAX_DATA)
  public void checkMaxTest(int nr1, int nr2, int expected) {
    Assert.fail("This code shouldnt be executed");
  }

  @DataProvider(name = CHECK_MIN_DATA)
  public Object[][] dataProviderCheckMin() {
    return new Object[][] {
      { 1, 2, },
    };
  }

  @Test(description = "Number of parameters to this test don't match the " +
      "ones passed by data provider", dataProvider = CHECK_MIN_DATA)
  public void checkMinTest(int nr1, int nr2, int expected) {
    Assert.fail("This code shouldnt be executed");
  }

  @Test(description = "Number of parameters to this test don't match the " +
        "ones passed by data provider. But an ojbect will be injected",
        dataProvider = CHECK_MIN_DATA)
  public void checkMinTest_injection(int nr1, int nr2, ITestContext ctx) {
    int result = Math.min(nr1, nr2);
    Assert.assertEquals(result, nr1);
    Assert.assertNotNull(ctx);
  }
}
