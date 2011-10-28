package test.bug92;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import test.SimpleBaseTest;

public class Bug92Test extends SimpleBaseTest {

	@Test(description = "Fix for https://github.com/cbeust/testng/issues/92")
	public void BeforeTestShouldRunOnce() {
		XmlSuite s = createXmlSuite("Bug92");
		XmlTest t = createXmlTest(s, "Bug92 test", TestAlpha.class.getName(),
				TestBravo.class.getName());
		s.setTests(Arrays.asList(t));
		TestNG tng = create();
		tng.setXmlSuites(Arrays.asList(s));
		TestBase.beforeTestCount = 0;
		TestBase.beforeTestAlwaysCount = 0;
		tng.run();
		Assert.assertEquals(TestBase.beforeTestCount, 1);
		Assert.assertEquals(TestBase.beforeTestAlwaysCount, 1);
	}
}
