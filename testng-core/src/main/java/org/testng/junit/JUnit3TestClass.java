package org.testng.junit;

import junit.framework.Test;

/**
 * @deprecated - Support for running JUnit tests stands deprecated as of TestNG <code>7.7.0</code>
 */
@Deprecated
public class JUnit3TestClass extends JUnitTestClass {

  public JUnit3TestClass(Test test) {
    super(test.getClass());
  }
}
