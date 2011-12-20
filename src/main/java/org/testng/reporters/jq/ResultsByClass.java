package org.testng.reporters.jq;

import org.testng.ITestResult;
import org.testng.collections.ListMultiMap;
import org.testng.collections.Maps;

import java.util.List;

public class ResultsByClass {
  private ListMultiMap<Class<?>, ITestResult> m_results = Maps.newListMultiMap();

  public void addResult(Class<?> c, ITestResult tr) {
    m_results.put(c, tr);
  }

  public List<ITestResult> getResults(Class<?> c) {
    return m_results.get(c);
  }
  public List<Class<?>> getClasses() {
    return m_results.getKeys();
  }
}
