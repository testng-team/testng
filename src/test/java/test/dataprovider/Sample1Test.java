package test.dataprovider;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class Sample1Test {
  private boolean m_ok1 = false;
  private boolean m_ok2 = false;

  private static final String FN2 = "Anne Marie";
  private static final Integer LN2 = 37;
  private static final String FN1 = "Cedric";
  private static final Integer LN1 = 36;

  @DataProvider(name = "test1")
  public Object[][] createData() {
    return new Object[][] {
        new Object[] { FN1, LN1 },
        new Object[] { FN2, LN2 },
      };
  }

  @Test(dataProvider = "test1")
  public void verifyNames(String firstName, Integer age) {
    if (firstName.equals(FN1) && age.equals(LN1)) {
      m_ok1 = true;
    }
    if (firstName.equals(FN2) && age.equals(LN2)) {
      m_ok2 = true;
    }
  }

  @Test(dependsOnMethods = {"verifyNames"})
  public void verifyCount() {
    Assert.assertTrue(m_ok1 && m_ok2);
  }
}
