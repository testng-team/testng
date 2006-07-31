package test.dataprovider;

import org.testng.Assert;


public class ChildDataProvider extends ParentBaseTest {
  static final String FN1 = "Alex";
  static final String FN2 = "Ana-Maria";
  
  /**
   * @testng.data-provider name="child1"
   */
  public Object[][] createData() {
    return new Object[][] {
        new Object[] { FN1 },
        new Object[] { FN2 },
      };
  }
  
  /**
   * @testng.test dependsOnMethods="useChildDataProvider"
   */
  public void verifyParentInvocation() {
    Assert.assertTrue(m_ok1 && m_ok2);
  }
}
