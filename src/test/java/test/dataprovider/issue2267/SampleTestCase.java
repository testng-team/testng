package test.dataprovider.issue2267;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

@Listeners(CustomListener.class)
public class SampleTestCase extends SimpleBaseTest {

  @DataProvider(name = "dp")
  public Object[][] dp() {
    return new Object[][] {
      {1},
    };
  }

  @Test(dataProvider = "dp")
  public void testWithRetryAndDataProvider(int testNumber) {
    Assert.fail("This time test FAIL! with testNumber: " + testNumber);
  }
}
