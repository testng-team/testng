package test.inheritance.issue2489.tests;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestClassA extends BaseClassA {

  @BeforeClass(groups = "a")
  public void beforeClass() {
    print("beforeClass_TestClass_A");
  }

  @AfterClass(groups = "a")
  public void afterClass() {
    print("afterClass_TestClass_A");
  }

  @BeforeMethod(groups = "a")
  public void beforeMethod() {
    print("beforeMethod_TestClass_A");
  }

  @AfterMethod(groups = "a")
  public void afterMethod() {
    print("afterMethod_TestClass_A");
  }

  @Test(groups = "a")
  public void test1() {
    print("test1_TestClass_A");
  }

  @Test(groups = "a")
  public void test2() {
    print("test2_TestClass_A");
  }
}
