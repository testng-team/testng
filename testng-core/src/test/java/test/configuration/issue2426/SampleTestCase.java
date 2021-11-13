package test.configuration.issue2426;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class SampleTestCase {

  @DataProvider(name = "constructorArguments")
  public static Object[][] constructorArguments() {
    return new Object[][] {
      {1, false},
      {2, true}
    };
  }

  @Factory(dataProvider = "constructorArguments")
  public SampleTestCase(int number, final boolean bool) {}

  @BeforeSuite
  public void beforeSuite() {}

  @BeforeTest
  public void beforeTest() {}

  @BeforeClass
  public void beforeClass() {}

  @BeforeMethod
  public void beforeMethod() {}

  @Test
  public void test() {}

  @AfterMethod
  public void afterMethod() {}

  @AfterClass
  public void afterClass() {}

  @AfterTest
  public void afterTest() {}

  @AfterSuite
  public void afterSuite() {}
}
