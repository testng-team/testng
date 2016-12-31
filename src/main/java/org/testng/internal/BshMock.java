package org.testng.internal;

import org.testng.ITestNGMethod;
import org.testng.TestNGException;

public class BshMock implements IBsh {

  @Override
  public boolean includeMethodFromExpression(String expression, ITestNGMethod tm) {
    String msg = "Beanshell related classes could not be found in CLASSPATH."
        + "Please add a dependency to bsh.Interpreter (beanshell jar) to your build file and try again.";
    throw new TestNGException(msg);
  }

}
