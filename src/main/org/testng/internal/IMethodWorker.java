package org.testng.internal;

import java.util.List;

import org.testng.ITestResult;


/**
 * Requirements for a method runnable.
 */
public interface IMethodWorker extends Runnable {
  long getMaxTimeOut();
  
  List<ITestResult> getTestResults();
}
