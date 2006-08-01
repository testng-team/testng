package test.sample;


public class BaseAfterClassCalledAtEnd {
   protected boolean m_afterClass = false;

   /**
    * @testng.configuration afterTestClass="true" dependsOnGroups=".*
    */
   public void baseAfterClass() {
      assert m_afterClass : "This afterClass method should have been called last";
   }
}
