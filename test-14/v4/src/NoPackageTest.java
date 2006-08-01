
/**
 * @author Filippo Diotalevi
 */
public class NoPackageTest {
   private boolean m_run = false;

   /**
    * @testng.test groups="nopackage"
    */
   public void test() {
      m_run = true;
   }

   /**
    * @testng.configuration afterTestMethod="true"
    *                       groups="nopackage"
    */
   public void afterTest() {
      assert m_run : "test method was not run";
   }
}
