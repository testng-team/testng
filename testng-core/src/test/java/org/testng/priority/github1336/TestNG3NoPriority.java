package org.testng.priority.github1336;

import org.testng.annotations.Test;
import org.testng.priority.samples.github1336.BaseClass;

public class TestNG3NoPriority extends BaseClass {
  @Test(description = "GITHUB-1336")
  public void test1TestNG3() {
    runTest("https://testng.org/doc/download.html");
  }

  @Test(description = "GITHUB-1336")
  public void test2TestNG3() {
    runTest("https://www3.lenovo.com/in/en/");
  }

  @Test(description = "GITHUB-1336")
  public void test3TestNG3() {
    runTest("https://github.com/");
  }
}
