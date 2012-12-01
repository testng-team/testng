package org.testng.internal;

import org.testng.IResultMap;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.collections.Objects;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ResultMap implements IResultMap {
  /**
   *
   */
  private static final long serialVersionUID = 80134376515999093L;
  private Map<ITestResult, ITestNGMethod> m_map = new ConcurrentHashMap<ITestResult, ITestNGMethod>();

  @Override
  public void addResult(ITestResult result, ITestNGMethod method) {
    m_map.put(result, method);
  }

  @Override
  public Set<ITestResult> getResults(ITestNGMethod method) {
    Set<ITestResult> result = new HashSet<ITestResult>();

    for (ITestResult tr : m_map.keySet()) {
      if (m_map.get(tr).equals(method)) {
        result.add(tr);
      }
    }

    return result;
  }

  @Override
  public void removeResult(ITestNGMethod m) {
    for (Entry<ITestResult, ITestNGMethod> entry : m_map.entrySet()) {
      if (entry.getValue().equals(m)) {
        m_map.remove(entry.getKey());
        return;
      }
    }
  }

  @Override
  public Set<ITestResult> getAllResults() {
    return m_map.keySet();
  }

  @Override
  public int size() {
    return m_map.size();
  }

  @Override
  public Collection<ITestNGMethod> getAllMethods() {
    return m_map.values();
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(getClass())
        .add("map", m_map)
        .toString();
  }
}
