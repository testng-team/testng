package org.testng;

import java.util.Collection;
import java.util.Set;

public interface IResultMap {

  void addResult(ITestResult result);

  Set<ITestResult> getResults(ITestNGMethod method);

  Set<ITestResult> getAllResults();

  void removeResult(ITestNGMethod m);

  void removeResult(ITestResult r);

  Collection<ITestNGMethod> getAllMethods();

  int size();
}
