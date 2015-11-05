package org.testng;

public interface IClassListener extends ITestNGListener {

  void onBeforeClass(ITestClass testClass, IMethodInstance mi);
  void onAfterClass(ITestClass testClass, IMethodInstance mi);
}
