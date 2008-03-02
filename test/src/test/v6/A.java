package test.v6;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class A {

  @Test(dependsOnMethods = "f1")
  public void f2() {
  }

  @Test(groups = "1")
  public void f1() {}
  
  @Test public void f3() {}
  
  @BeforeGroups("1")
  public void beforeGroups() {}
  
  @AfterGroups("1")
  public void afterGroups() {}

  @BeforeMethod
  public void beforeMethod() {}
  
  @AfterMethod
  public void afterMethod() {}
  
  @BeforeSuite
  public void beforeSuite() {}
  
  @AfterSuite
  public void afterSuite() {}
  
  @BeforeClass
  public void beforeClass() {}
  
  @AfterClass
  public void afterClass() {}
}
