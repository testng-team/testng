package org.testng.reporters.jq;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.testng.ITestResult;
import org.testng.collections.ListMultiMap;
import org.testng.collections.Maps;

public class ResultsByClass {
  public static final Comparator<ITestResult> METHOD_NAME_COMPARATOR =
      Comparator.comparing(arg0 -> arg0.getMethod().getMethodName());

  private final ListMultiMap<Class<?>, ITestResult> m_results = Maps.newListMultiMap();

  public void addResult(Class<?> c, ITestResult tr) {
    m_results.put(c, tr);
  }

  public List<ITestResult> getResults(Class<?> c) {
    List<ITestResult> result = m_results.get(c);
    result.sort(METHOD_NAME_COMPARATOR);
    return result;
  }

  public Set<Class<?>> getClasses() {
    return m_results.keySet();
  }
}
