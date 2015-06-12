package test.dataprovider;

import static org.testng.Assert.assertEquals;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

public class ExplicitDataProviderNameTest extends SimpleBaseTest {

	@Test(description = "TESTNG-576: Prefer DataProvider explicit name")
	public void should_prefer_dataProvider_explicit_name() {
	    TestNG testng = create(ExplicitDataProviderNameSample.class);
	    TestListenerAdapter tla = new TestListenerAdapter();
	    testng.addListener(tla);
	    testng.run();
	    
	    assertEquals(tla.getPassedTests().size(), 1, "All tests should success");
	  }
}