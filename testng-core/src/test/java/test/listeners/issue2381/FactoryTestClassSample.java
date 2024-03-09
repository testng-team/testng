package test.listeners.issue2381;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class FactoryTestClassSample {

  @Factory(dataProvider = "dp")
  public FactoryTestClassSample(int ignored) {}

  @DataProvider
  public static Object[][] dp() {
    return new Object[][] {{1}};
  }

  @BeforeSuite
  public void beforeSuite() {}

  @Test
  public void testMethod() {}

  @AfterSuite
  public void afterSuite() {}
}
