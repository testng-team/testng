package test.sample;

/*
 * Created on 12-Sep-2006 by micheb10
 */

/**
 * Sample file for the Javadoc annotations to Java 5 annotations converter for a non-default package
 * @author micheb10 12-Sep-2006
 * @testng.test
 */
public class ConverterSample3 {
	/**
	 * This comment line should be preserved
	 * @testng.before-suite alwaysRun = "true"
	 */
	public void beforeSuiteAlwaysRun() {
		// We are just checking appropriate annotations are added so we don't care about body
	}

	/**
	 * @testng.test
	 */
	public void plainTest() {
		// Empty body
	}

	/**
	 * @testng.test
	 * @testng.expected-exceptions
	 * value = "java.lang.NullPointerException java.lang.NumberFormatException"
	 */
	public void expectedExceptions() {
		// Empty body
	}

	/**
	 * @testng.test groups = "groupA groupB"
	 */
	public void testGroups() {
		// Empty body
	}

	/**
	 * @testng.after-method
	 */
	public void afterMethod() {
		// Empty body
	}

	/**
	 * This key should be preserved
	 * @author The author is a standard tag and should not be touched
	 * @testng.test groups = "groupA" alwaysRun=true
	 * 		dependsOnMethods = "expectedExceptions" timeOut="3000"
	 * @version another standard tag should not be changed
	 * @testng.expected-exceptions
	 * value = "java.lang.NullPointerException java.lang.NumberFormatException"
	 * @testng.parameters value="firstParameter secondParameter thirdParameter"
	 */
	public void testEverything() {
		// Lots and lots of stuff
	}

	/**
	 * @testng.data-provider name="test1"
	 */
	public Object[][] dataProvider() {
		return null;
	}

	/**
	 * @testng.factory
	 */
	public Object[] factory() {
		return null;
	}

	/**
	 * @testng.test
	 */
	public class TestInnerClass {
		public void bareInnerMethod() {
			// Empty body
		}

		/**
		 * @testng.test
		 */
		public void testInnerMethod() {
			// empty body
		}
	}
}
