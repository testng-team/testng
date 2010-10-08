package test.v6;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class B {

  @Test(dependsOnMethods = "fb1")
  public void fb2() {
  }

  @Test(groups = "1")
  public void fb1() {}

  @Test public void fb3() {}

  @BeforeMethod
  public void beforeMethod() {}

  @AfterMethod(groups = "1")
  public void afterMethod() {}

  @BeforeSuite
  public void beforeSuite() {}

  @BeforeClass
  public void beforeClass() {}

  @AfterClass
  public void afterClass() {}
}
