package test.dataprovider;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ExplicitDataProviderNameSample {
	
	@Test(dataProvider="dp_name")
	public void should_find_exactly_one_data_provider(int i) {}
	
	@DataProvider(name="dp_name")
	Object[][] whatever_implicit_name() {return new Object[][] {{1}};}
	
	@DataProvider(name="whatever_explicit_name")
	Object[][] dp_name() {return null;}
}