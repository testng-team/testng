
import org.testng.annotations.Test;
import org.testng.annotations.Configuration;

/**
 * @author Filippo Diotalevi
 */
public class NoPackageTest {
	private boolean m_run = false;

	@Test(groups = {"nopackage"})
	public void test() {
	   m_run = true;
	}

   @Configuration(afterTestMethod = true, groups = {"nopackage"})
   public void after() {
      assert m_run : "test method was not run";
   }
}
