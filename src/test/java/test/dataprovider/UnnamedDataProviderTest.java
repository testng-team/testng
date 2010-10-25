package test.dataprovider;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


/**
 * This class/interface
 */
public class UnnamedDataProviderTest {
  private boolean m_false = false;
  private boolean m_true = false;

  @Test(dataProvider = "unnamedDataProvider")
  public void doStuff(boolean t) {
    if (t) {
      m_true = true;
    }
    if (! t) {
      m_false = true;
    }
  }

  @Test(dependsOnMethods = {"doStuff"} )
  public void verify() {
    Assert.assertTrue(m_true);
    Assert.assertTrue(m_false);
  }

  @DataProvider
  public Object[][] unnamedDataProvider() {
    return new Object[][] {
        {Boolean.TRUE},
        {Boolean.FALSE}
    };
  }
}
