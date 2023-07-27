package org.testng;


public interface IListenerSetupReporter extends ITestNGListener {

  default void onListenerSetupStart() {
    // not implemented
  }

  default void onListenerSetupFailure(Throwable e) {
    // not implemented
  }

  default void onListenerSetupFinish() {
    // not implemented
  }
}
