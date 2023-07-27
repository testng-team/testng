package org.testng.xml.issue2937;

import org.testng.IListenerSetupReporter;

public class ListenerSetupReporter implements IListenerSetupReporter {
  public static boolean isListenerSetupFailureReported = false;

  @Override
   public void onListenerSetupStart() {
    System.out.println("onListenerSetupStart");
  }

  @Override
  public void onListenerSetupFailure(Throwable e) {
    isListenerSetupFailureReported = true;
    System.out.println("onListenerSetupFailure");
  }

  @Override
  public void onListenerSetupFinish() {

    System.out.println("onListenerSetupFinish");
  }
}
