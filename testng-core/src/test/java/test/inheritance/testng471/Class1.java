package test.inheritance.testng471;

import org.junit.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Class1 extends SuperClass1 {

  @BeforeClass
  public void beforeClass1() {}

  @AfterClass
  public void afterClass1() {}

  @BeforeMethod
  public void beforeMethodClass1() {}

  @AfterMethod
  public void afterMethodClass1() {
    Assert.fail();
  }

  @Test
  public void test1_1() {}

  @Test
  public void test1_2() {}
}
