package org.testng;

/**
 * This class is used by TestNG to locate the test classes.
 *
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public interface ITestClassFinder {
  /**
   * @return An array of all the classes that contain test methods. This method usually returns an
   *     array of one class, which is the class on which TestNG is running, except in the following
   *     cases. - TestNG: the class contains an @Factory method - JUnit: the class contains a
   *     suite() method
   */
  IClass[] findTestClasses();

  /**
   * Return the IClass for a given class
   *
   * @param cls The class
   * @return The related <code>IClass</code>
   */
  IClass getIClass(Class<?> cls);
}
