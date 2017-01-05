package org.testng.internal;

import org.testng.ITestNGMethod;
import org.testng.TestNGException;

public class BshMock implements IBsh {

  @Override
  public boolean includeMethodFromExpression(String expression, ITestNGMethod tm) {
    String msg = "Beanshell related classes could not be found in CLASSPATH."
        + "Please add a compile dependency to 'org.apache-extras.beanshell:bsh:2.0b6' in your build file and try "
        + "again.";
    throw new TestNGException(msg);
  }

}
