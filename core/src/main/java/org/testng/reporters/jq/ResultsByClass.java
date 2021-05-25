package org.testng.reporters.jq;

import org.testng.ITestResult;
import org.testng.collections.ListMultiMap;
import org.testng.collections.Maps;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class ResultsByClass {
  public static final Comparator<ITestResult> METHOD_NAME_COMPARATOR =
      new Comparator<ITestResult>() {

        @Override
        public int compare(ITestResult arg0, ITestResult arg1) {
          return arg0.getMethod().getMethodName().compareTo(arg1.getMethod().getMethodName());
        }
      };

  private ListMultiMap<Class<?>, ITestResult> m_results = Maps.newListMultiMap();

  public void addResult(Class<?> c, ITestResult tr) {
    m_results.put(c, tr);
  }

  public List<ITestResult> getResults(Class<?> c) {
    List<ITestResult> result = m_results.get(c);
    Collections.sort(result, METHOD_NAME_COMPARATOR);
    return result;
  }

  public Set<Class<?>> getClasses() {
    return m_results.keySet();
  }
}
