package org.testng.internal;

import org.testng.ITestNGMethod;

public interface IBsh {
  boolean includeMethodFromExpression(String expression, ITestNGMethod tm);
}
