package test.retryAnalyzer.issue1946;

import org.testng.annotations.Test;

public class TestclassSample1 extends TestclassBase {

  @Test(dataProvider = "dp", retryAnalyzer = RetryAnalyzer.class)
  public void test1(String username, String password) {
    performTest(username, password);
  }

  @Test(dataProvider = "dp", retryAnalyzer = RetryAnalyzer.class)
  public void test2(String username, String password) {
    performTest(username, password);
  }

}
