package test.dataprovider;

import org.testng.Assert;


public class ChildUsingParentDataProviderTest extends AbstractRootDataProvider {
  /**
   * @testng.test dataProvider="test1"
   */
  public void useParentDataProvider(String firstName, Integer age) {
    if (firstName.equals(FN1) && age.equals(LN1)) {
      m_ok3 = true;
    }
    if (firstName.equals(FN2) && age.equals(LN2)) {
      m_ok4 = true;
    }
  }
  
  /**
   * @testng.test dependsOnMethods = "useParentDataProvider"
   */
  public void verifyCount() {
    Assert.assertTrue(m_ok3 && m_ok4);
  }
}
