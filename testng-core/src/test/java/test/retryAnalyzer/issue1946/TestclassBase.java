package test.retryAnalyzer.issue1946;

import static org.testng.Assert.fail;

import org.testng.Reporter;
import org.testng.annotations.DataProvider;

public class TestclassBase {

  @DataProvider
  public Object[][] dp() {
    return new Object[][] {
      {"param1", "value1"},
      {"param2", "value2"}
    };
  }

  static void performTest(String username, String password) {
    String method = Reporter.getCurrentTestResult().getMethod().getMethodName();
    String txt = String.format("%s() Assertion for (%s, %s)", method, username, password);
    fail(txt);
  }
}
