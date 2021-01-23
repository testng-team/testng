package test.inheritance.testng471;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Class3 extends SuperClass2 {

  @BeforeClass
  public void beforeClass3() {
  }

  @AfterClass
  public void afterClass3() {
  }

  @BeforeMethod
  public void beforeMethodClass3() {
  }

  @AfterMethod
  public void afterMethodClass3() {
  }

  @Test
  public void test3_1() {
  }

  @Test
  public void test3_2() {
  }
}
