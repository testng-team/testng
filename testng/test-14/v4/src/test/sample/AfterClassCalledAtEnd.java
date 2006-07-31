package test.sample;


/**
 * Check to see that AfterClass is called only at the end and that after methods
 * are called in reverse order of the before methods.
 */
public class AfterClassCalledAtEnd extends BaseAfterClassCalledAtEnd {
   boolean m_before1Class = false;
   boolean m_test1 = false;
   boolean m_test2 = false;
   boolean m_test3 = false;

   /**
    * @testng.configuration beforeTestClass="true" groups="before1Class"
    */
   public void before1Class() {
      m_before1Class = true;
   }

   /**
    * @testng.configuration afterTestClass="true" groups="someGroup"
    */
   public void afterClass() {
      m_afterClass = true;
      assert m_test1 && m_test2 && m_test3 :
            "One of the test methods was not invoked: " + m_test1 + " " + m_test2 + " " + m_test3;
   }

   /**
    * @testng.test
    */
   public void test1() {
      m_test1 = true;
      assert m_before1Class : "beforeClass configuration must be called before method";
      assert !m_afterClass : "afterClass configuration must not be called before test method";
   }

   /**
    * @testng.test
    */
   public void test2() {
      m_test2 = true;
      assert m_before1Class : "beforeClass configuration must be called before method";
      assert !m_afterClass : "afterClass configuration must not be called before test method";
   }

   /**
    * @testng.test
    */
   public void test3() {
      m_test3 = true;
      assert m_before1Class : "beforeClass configuration must be called before method";
      assert !m_afterClass : "afterClass configuration must not be called before test method";
   }

}