package org.testng.junit;

/**
 * @deprecated - Support for running JUnit tests stands deprecated as of TestNG <code>7.6.2</code>
 */
@Deprecated
interface JUnitTestRecognizer {

  boolean isTest(Class c);
}
