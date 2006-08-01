package test.simple;

import org.testng.Reporter;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

public class IncludedExcludedSampleTest {
  
  @Configuration(beforeSuite = true)
  public void beforeSuite() {
    Reporter.log("beforeSuite");
  }

  @Configuration(beforeTest = true)
  public void beforeTest() {
    Reporter.log("beforeTest");
  }

  @Configuration(beforeTestClass = true)
  public void beforeTestClass() {
    Reporter.log("beforeTestClass");
  }

  @Configuration(beforeTestMethod = true)
  public void beforeTestMethod() {
    Reporter.log("beforeTestMethod");
  }

  @Test
  public void test1() {
    Reporter.log("Child.test1");
  }

  @Test(enabled = false)
  public void test2() {
    Reporter.log("Child.test2");
  }
  
  @Test(groups = "a")
  public void test3() {
    Reporter.log("Child.test3");
  }

  private void ppp(String string) {
    System.out.println("[TestNGBug] " + string);
  }
}
