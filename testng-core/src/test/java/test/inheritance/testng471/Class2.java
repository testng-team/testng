package test.inheritance.testng471;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Class2 extends SuperClass1 {

  @BeforeClass
  public void beforeClass2() {}

  @AfterClass
  public void afterClass2() {}

  @BeforeMethod
  public void beforeMethodClass2() {}

  @AfterMethod
  public void afterMethodClass2() {}

  @Test
  public void test2_1() {}

  @Test
  public void test2_2() {}
}
