package org.testng;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

public interface IResultMap extends Serializable {

  public void addResult(ITestResult result, ITestNGMethod method);

  public Set<ITestResult> getResults(ITestNGMethod method);

  public Set<ITestResult> getAllResults();

  public void removeResult(ITestNGMethod m);

  public void removeResult(ITestResult r);

  public Collection<ITestNGMethod> getAllMethods();

  public int size();

}
