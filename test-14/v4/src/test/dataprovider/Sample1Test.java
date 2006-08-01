package test.dataprovider;

import org.testng.Assert;

public class Sample1Test {
  private boolean m_ok1 = false;
  private boolean m_ok2 = false;
  private boolean m_ok3 = false; 

  private static final String FN2 = "Anne Marie";
  private static final Integer LN2 = new Integer(37);
  private static final String FN1 = "Cedric";
  private static final Integer LN1 = new Integer(36);
  private static final String FN3 = "Alex";
  private static final Integer LN3 = new Integer(30);
  
  /**
   * @testng.data-provider name="test1"
   */
  public Object[][] createData() {
    return new Object[][] {
        new Object[] { FN1, LN1 },
        new Object[] { FN2, LN2 },
        new Object[] { FN3, LN3 }
      };
  }
  
  /**
   * @testng.test dataProvider="test1"
   */
  public void verifyNames(String firstName, Integer age) {
    if (firstName.equals(FN1) && age.equals(LN1)) {
      m_ok1 = true;
    }
    if (firstName.equals(FN2) && age.equals(LN2)) {
      m_ok2 = true;
    }
    if (firstName.equals(FN3) && age.equals(LN3)) {
      m_ok3 = true;
    }
  }
  
  /**
   * @testng.test dependsOnMethods = "verifyNames"
   */
  public void verifyCount() {
    Assert.assertTrue(m_ok1 && m_ok2 && m_ok3);
  }
}
