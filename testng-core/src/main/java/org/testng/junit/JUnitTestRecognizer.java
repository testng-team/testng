package org.testng.junit;

/**
 * @deprecated - Support for running JUnit tests stands deprecated as of TestNG <code>7.7.0</code>
 */
@Deprecated
interface JUnitTestRecognizer {

  boolean isTest(Class c);
}
