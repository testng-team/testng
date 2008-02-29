package test.v6;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class B {

  @Test(dependsOnMethods = "f1")
  public void f2() {
  }

  @Test
  public void f1() {}
  
  @Test public void f3() {}

  @BeforeMethod
  public void beforeMethod() {}
  
  @AfterMethod
  public void afterMethod() {}
  
  @BeforeSuite
  public void beforeSuite() {}
  
  @BeforeClass
  public void beforeClass() {}
  
  @AfterClass
  public void afterClass() {}
}
