package org.testng;

public interface IClassListener extends ITestNGListener {

  void onBeforeClass(ITestClass testClass);
  void onAfterClass(ITestClass testClass);
}
