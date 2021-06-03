
/*
 * Created on 12-Sep-2006 by micheb10
 * it is at the wrong location, but it's easier to leave it here.
 * Also, do not change the line numbers since the test will make sure
 * that the tags are generated in hardcoded line numbers
 */

/**
 * Sample file for the Javadocv annotations to Java 5 annotations converter for the default package
 * @author micheb10 12-Sep-2006
 *
 */
public class ConverterSample4 {
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
	 * @testng.test groups = "groupA"
	 * 		dependsOnMethods = "expectedExceptions" timeOut="3000" unrecognised="futureProof"
	 * @version another standard tag should not be changed
	 * @testng.expected-exceptions
	 * value = "java.lang.NullPointerException java.lang.NumberFormatException"
	 *
	 */
	public void testEverything() {

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
	@SuppressWarnings({"unchecked", "deprecation"})
	public Object[] factory() {
		return null;
	}

}
