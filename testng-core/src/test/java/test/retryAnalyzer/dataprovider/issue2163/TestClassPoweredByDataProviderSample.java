package test.retryAnalyzer.dataprovider.issue2163;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestClassPoweredByDataProviderSample {

  @Test(retryAnalyzer = RetryAnalyzer.class, dataProvider = "dpNewObject")
  public void willNotStopAfter3Failures(Object... newObject) {
    Assert.fail("Kaboom!");
  }

  @DataProvider
  public Object[][] dpNewObject() {
    return new Object[][] {
      {"String as parameter"},
      {"int as parameter", 123},
      {"boolean as parameter", true},
      {"Boolean as parameter", true},
      {"null parameter", null},
      {new Object()}, // GITHUB-2163
      {"GITHUB-2280", new Object[] {new Object()}},
    };
  }
}
