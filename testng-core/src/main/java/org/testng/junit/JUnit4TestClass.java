package org.testng.junit;

import org.junit.runner.Description;

/**
 * @deprecated - Support for running JUnit tests stands deprecated as of TestNG <code>7.7.0</code>
 */
@Deprecated
public class JUnit4TestClass extends JUnitTestClass {

  public JUnit4TestClass(Description test) {
    super(descriptionToClass(test));
  }

  private static Class<?> descriptionToClass(Description test) {
    Class<?> result = test.getTestClass();
    if (result == null) {
      result = org.testng.internal.ClassHelper.forName(test.getClassName());
    }
    return result;
  }
}
