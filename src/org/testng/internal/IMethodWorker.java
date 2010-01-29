package org.testng.internal;

import org.testng.ITestNGMethod;
import org.testng.ITestResult;

import java.util.List;


/**
 * Requirements for a method runnable.
 */
public interface IMethodWorker extends Runnable, Comparable<IMethodWorker> {
  long getMaxTimeOut();
  
  List<ITestResult> getTestResults();

  List<ITestNGMethod> getMethods();

  int getPriority();
}
