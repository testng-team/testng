package org.testng;

import java.util.Collection;
import java.util.Set;

public interface IResultMap {

  /**
   * @deprecated - This method stands deprecated as of 7.4.0
   */
  @Deprecated
  void addResult(ITestResult result, ITestNGMethod method);

  void addResult(ITestResult result);

  Set<ITestResult> getResults(ITestNGMethod method);

  Set<ITestResult> getAllResults();

  void removeResult(ITestNGMethod m);

  void removeResult(ITestResult r);

  Collection<ITestNGMethod> getAllMethods();

  int size();
}
