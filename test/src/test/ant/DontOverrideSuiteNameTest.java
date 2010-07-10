package test.ant;

import org.testng.annotations.Test;

@Test
public class DontOverrideSuiteNameTest {
	private boolean m_run = false;

	@Test(groups = {"nopackage"})
	public void test() {
	   m_run = true;
	}
}
