package test.invokedmethodlistener;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class Base {
  private boolean m_fail;

  public Base(boolean fail) {
    m_fail = fail;
  }

  @BeforeMethod
  public void beforeMethod() {
  }

  @AfterMethod
  public void afterMethod() {}

  @BeforeTest
  public void beforeTest() {}

  @AfterTest
  public void afterTest() {}

  @BeforeClass
  public void beforeClass() {}

  @AfterClass
  public void afterClass() {}

  @BeforeSuite
  public void beforeSuite() {}

  @AfterSuite
  public void afterSuite() {
    if (m_fail) {
      throw new RuntimeException("After Suite FAILING");
    }
  }

  @Test
  public void a() {
     if (m_fail) {
      throw new IllegalArgumentException("Test Method FAILING");
    }
  }

}
