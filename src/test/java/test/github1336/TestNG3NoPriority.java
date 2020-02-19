package test.github1336;

import org.testng.annotations.Test;

public class TestNG3NoPriority extends BaseClass {
  @Test
  public void test1TestNG3() {
    runTest("https://testng.org/doc/download.html");
  }

  @Test
  public void test2TestNG3() {
    runTest("https://www3.lenovo.com/in/en/");
  }

  @Test
  public void test3TestNG3() {
    runTest("https://github.com/");
  }
}
