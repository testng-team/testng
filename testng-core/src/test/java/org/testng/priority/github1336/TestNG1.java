package org.testng.priority.github1336;

import org.testng.annotations.Test;
import org.testng.priority.samples.github1336.BaseClass;

public class TestNG1 extends BaseClass {
  @Test(priority = 1, description = "GITHUB-1336")
  public void test1TestNG1() {
    runTest("https://testng.org/doc/download.html");
  }

  @Test(priority = 2, description = "GITHUB-1336")
  public void test2TestNG1() {
    runTest("https://www3.lenovo.com/in/en/");
  }

  @Test(priority = 3, description = "GITHUB-1336")
  public void test3TestNG1() {
    runTest("https://github.com/");
  }
}
