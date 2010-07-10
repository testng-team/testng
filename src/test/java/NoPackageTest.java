
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

/**
 * @author Filippo Diotalevi
 */
public class NoPackageTest {
	private boolean m_run = false;

	@Test(groups = {"nopackage"})
	public void test() {
	   m_run = true;
	}

   @AfterMethod(groups = {"nopackage"})
   public void after() {
      assert m_run : "test method was not run";
   }
}
