package org.testng.internal;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.testng.IResultMap;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.collections.Objects;

public class ResultMap implements IResultMap {

  private final Set<ITestResult> results = ConcurrentHashMap.newKeySet();

  @Override
  public void addResult(ITestResult result) {
    results.add(result);
  }

  @Override
  public Set<ITestResult> getResults(ITestNGMethod method) {
    return results.stream()
        .filter(result -> result.getMethod().equals(method))
        .collect(Collectors.toSet());
  }

  @Override
  public void removeResult(ITestNGMethod m) {
    Set<ITestResult> toRemove = getResults(m);
    results.removeAll(toRemove);
  }

  @Override
  public void removeResult(ITestResult r) {
    results.remove(r);
  }

  @Override
  public Set<ITestResult> getAllResults() {
    return results;
  }

  @Override
  public int size() {
    return results.size();
  }

  @Override
  public Collection<ITestNGMethod> getAllMethods() {
    return results.stream().map(ITestResult::getMethod).collect(Collectors.toSet());
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(getClass()).add("map", results).toString();
  }
}
