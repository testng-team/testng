package test.configuration;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * Make sure that @BeforeTest is only called once if a factory is used
 *
 * @author Cedric Beust <cedric@beust.com>
 */
public class SingleConfigurationTest {

  private static int m_before;

  @Factory(dataProvider = "dp")
  public SingleConfigurationTest(int n) {
  }

  @DataProvider
  public static Object[][] dp() {
    return new Object[][] {
        new Object[] { 42 },
        new Object[] { 43 },
    };
  }

  @BeforeTest
  public void bt() {
    m_before++;
  }

  @Test
  public void verify() {
    Assert.assertEquals(m_before, 1);
  }
}
