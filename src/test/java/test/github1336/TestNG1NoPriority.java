package test.github1336;

import org.testng.annotations.Test;

public class TestNG1NoPriority extends BaseClass {
  @Test
  public void test1TestNG1() {
    runTest("http://testng.org/doc/download.html");
  }

  @Test
  public void test2TestNG1() {
    runTest("http://www3.lenovo.com/in/en/");
  }

  @Test
  public void test3TestNG1() {
    runTest("https://github.com/");
  }
}
