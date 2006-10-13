
import org.testng.annotations.Test;

/**
 * @author Filippo Diotalevi
 */
@Test
public class NoPackageTest {
	private boolean m_run = false;

	@Test(groups = {"nopackage"})
	public void test() {
	   m_run = true;
	}
}
