package test.dataprovider;

import org.testng.Assert;


public class MultipleDataProviderTest {
  private int m_countTest1;
  private boolean m_okeTest1;
  private int m_countTest2;
  private boolean m_okeTest2;
  
  /**
   * This method will provide data to any test method that declares that
   * its Data Provider is named "test1"
   *
   * @testng.data-provider name="test1"
   */
  public Object[][] createData1() {
    Object[][] inputs= new Object[][] { 
        new Object[] { "Name1", new Integer(46), new Integer(1) }, 
        new Object[] { "Name2", new Integer(47), new Integer(2) }, 
        new Object[] { "Name3", new Integer(43), new Integer(3) } 
    };

    return inputs;
  }

  /**
   * This test method declares that its data should be supplied by the
   * Data Provider named "test1"
   *
   * @testng.test groups="mytest1" dataProvider="test1"
   */
  public void verifyData1(String n1, Integer n2, Integer n3) {
    m_countTest1++;
    if (n3.equals(new Integer(1)) || n3.equals(new Integer(2)) || n3.equals(new Integer(3))) {
      m_okeTest1 = true;
    }
    else {
      m_okeTest1 = false;
    }
  }

  /**
   * @testng.test dependsOnMethods="verifyData1"
   */
  public void verifyTest1DataProvider() {
    Assert.assertEquals(m_countTest1, 3, "verifyData1 should have been invoked 3 times," +
            "as per its @DataProvider");
    Assert.assertTrue(m_okeTest1, "looks like another @DataProvider has been used");
  }
  
  /**
   * This method will provide data to any test method that declares that
   * its Data Provider is named "test2"
   *
   * @testng.data-provider name="test2"
   */
  public Object[][] createData2() {
    Object[][] inputs= new Object[][] {
        new Object[] { "Cedric", new Integer(36), new Integer(1) }, 
        new Object[] { "Anne", new Integer(37), new Integer(2) }, 
        new Object[] { "John", new Integer(23), new Integer(3) }, 
        new Object[] { "Mary", new Integer(13), new Integer(4) }
      };

    return inputs;
  }

  /**
   * This test method declares that its data should be supplied by the
   * Data Provider named "test2"
   *
   * @testng.test groups="mytest2" dataProvider="test2"
   */
  public void verifyData2(String n1, Integer n2, Integer n3) {
    m_countTest2++;
    if ("Cedric".equals(n1) || "Anne".equals(n1) || "John".equals(n1) || "Mary".equals(n1)) {
      m_okeTest2 = true;
    }
    else {
      m_okeTest2 = false;
    }
  }
  
  /**
   * @testng.test dependsOnMethods="verifyData2"
   */
  public void verifyTest2DataProvider() {
    Assert.assertEquals(m_countTest2, 4, "verifyData2 should have been invoked 4 times," +
            "as per its @DataProvider");
    Assert.assertTrue(m_okeTest2, "looks like another @DataProvider has been used");
  }
}
