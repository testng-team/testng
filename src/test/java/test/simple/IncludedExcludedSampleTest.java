package test.simple;

import org.testng.Reporter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class IncludedExcludedSampleTest {

  @BeforeSuite
  public void beforeSuite() {
    Reporter.log("beforeSuite");
  }

  @BeforeTest
  public void beforeTest() {
    Reporter.log("beforeTest");
  }

  @BeforeClass
  public void beforeTestClass() {
    Reporter.log("beforeTestClass");
  }

  @BeforeMethod
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
