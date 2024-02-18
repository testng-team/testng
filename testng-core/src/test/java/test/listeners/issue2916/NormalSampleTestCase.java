package test.listeners.issue2916;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class NormalSampleTestCase {
  @BeforeSuite
  public void beforeSuite() {}

  @BeforeTest
  public void beforeTest() {}

  @BeforeClass
  public void beforeClass() {}

  @BeforeMethod
  public void beforeMethod() {}

  @AfterMethod
  public void afterMethod() {}

  @Test(dataProvider = "dp", priority = 1)
  public void testMethod(int ignored) {}

  @DataProvider(name = "dp")
  public Object[][] getData() {
    return new Object[][] {{1}, {2}};
  }

  @AfterClass
  public void afterClass() {}

  @AfterTest
  public void afterTest() {}

  @AfterSuite
  public void afterSuite() {}
}
