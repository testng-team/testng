package org.testng.junit;

import org.junit.runner.Description;

/** @author lukas */
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
