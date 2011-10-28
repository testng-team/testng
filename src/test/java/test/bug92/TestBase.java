package test.bug92;

import org.testng.annotations.BeforeTest;

public class TestBase {

	static int beforeTestCount = 0;
	static int beforeTestAlwaysCount = 0;

	@BeforeTest
	public void baseTestBeforeTest() {
		beforeTestCount++;
	}

	@BeforeTest(alwaysRun = true)
	public void baseTestBeforeTestAlways() {
		beforeTestAlwaysCount++;
	}

}