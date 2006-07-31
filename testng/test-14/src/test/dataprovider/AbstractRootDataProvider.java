package test.dataprovider;

import org.testng.Assert;


public abstract class AbstractRootDataProvider {
    protected boolean m_ok1 = false;
    protected boolean m_ok2 = false;
    protected boolean m_ok3 = false;
    protected boolean m_ok4 = false;

    static final String FN2 = "Anne Marie";
    static final Integer LN2 = new Integer(37);
    static final String FN1 = "Cedric";
    static final Integer LN1 = new Integer(36);


    /**
     * @testng.data-provider name="test1"
     */
    public Object[][] createData() {
      return new Object[][] {
          new Object[] { FN1, LN1 },
          new Object[] { FN2, LN2 },
        };
    }

    /**
     * @testng.test dataProvider="test1"
     */
    public void parentTest(String firstName, Integer age) {
      if (firstName.equals(FN1) && age.equals(LN1)) {
        m_ok1 = true;
      }
      if (firstName.equals(FN2) && age.equals(LN2)) {
        m_ok2 = true;
      }
    }
    
    
    /**
     * @testng.test dependsOnMethods = "parentTest"
     */
    public void verifyParentCount() {
      Assert.assertTrue(m_ok1 && m_ok2);
    }
}
