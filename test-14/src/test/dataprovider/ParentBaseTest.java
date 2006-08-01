package test.dataprovider;


public class ParentBaseTest {
  protected boolean m_ok1 = false;
  protected boolean m_ok2 = false;
  
  /**
   * @testng.test dataProvider="child1"
   */
  public void useChildDataProvider(String name) {
    if(name.equals(ChildDataProvider.FN1)) {
      m_ok1= true;
    }
    if(name.equals(ChildDataProvider.FN2)) {
      m_ok2= true;
    }
  }
}
