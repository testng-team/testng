package test.dataprovider;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DataProviderRetryTest {
  @Test(dataProvider = "getVerdic", retryAnalyzer = DataProviderRetryAnalyzer.class)
  public void test(String verdict) {
    switch (verdict) {
      case "FAIL":
        Assert.fail("This time test FAIL!");
        break;
      case "SKIP":
        throw new SkipException("This time test SKIPPED!");
      default:
        break;
    }
  }

  @DataProvider(name = "getVerdic")
  public Object[][] getVerdicNames() {
    return new Object[][] {{"FAIL"}, {"SKIP"}};
  }
}
