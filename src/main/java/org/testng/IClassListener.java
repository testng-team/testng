package org.testng;

public interface IClassListener extends ITestNGListener {

  default void onBeforeClass(ITestClass testClass) {
    // not implemented
  }

  default void onAfterClass(ITestClass testClass) {
    // not implemented
  }
}
