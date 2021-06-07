package test.retryAnalyzer.dataprovider;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public final class RetryCountTest {

  @DataProvider
  public Object[][] provider() {
    return new Object[][] {{"a"}, {"b"}, {"c"}, {"d"}};
  }

  @Test(dataProvider = "provider", retryAnalyzer = DataProviderRetryAnalyzer.class)
  public void test1(String param) {
    assertEquals(param, "c");
  }
}
