package org.testng.priority.github1336;

import org.testng.annotations.Test;
import org.testng.priority.samples.github1336.BaseClass;

public class TestNG2NoPriority extends BaseClass {
  @Test(description = "GITHUB-1336")
  public void test1TestNG2() {
    runTest("https://testng.org/doc/download.html");
  }

  @Test(description = "GITHUB-1336")
  public void test2TestNG2() {
    runTest("https://www3.lenovo.com/in/en/");
  }

  @Test(description = "GITHUB-1336")
  public void test3TestNG2() {
    runTest("https://github.com/");
  }
}
