package test.retryAnalyzer.issue1946;

import org.testng.annotations.Test;

public class TestclassSample2 extends TestclassBase {

  @Test(dataProvider = "dp", retryAnalyzer = RetryAnalyzer.class)
  public void test1(String username, String password) {
    performTest(username, password);
  }

  @Test(dataProvider = "dp", retryAnalyzer = RetryAnalyzer.class)
  public void test3(String username, String password) {
    performTest(username, password);
  }

  @Test(dataProvider = "dp", retryAnalyzer = RetryAnalyzer.class)
  public void test4(String username, String password) {
    performTest(username, password);
  }
}
