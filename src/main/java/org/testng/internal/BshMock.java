package org.testng.internal;

import org.testng.ITestNGMethod;

public class BshMock implements IBsh {

  @Override
  public boolean includeMethodFromExpression(String expression, ITestNGMethod tm) {
    return false;
  }

}
