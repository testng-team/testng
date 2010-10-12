package test.dataprovider;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class BooleanTest {
  private boolean m_false = false;
  private boolean m_true = false;

  @Test(dataProvider = "allBooleans")
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

  private void ppp(String string) {
    System.out.println("[BooleanTest] " + string);
  }

  @DataProvider(name = "allBooleans")
  public Object[][] createData() {
    return new Object[][] {
      new Object[] { true },
      new Object[] { false },
    };
  }

}
