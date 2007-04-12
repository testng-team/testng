package org.testng.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;

import org.testng.IResultMap;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

public class ResultMap implements IResultMap {
  private Map<ITestResult, ITestNGMethod> m_map = new ConcurrentHashMap/*<ITestResult, ITestNGMethod>*/();

  public void addResult(ITestResult result, ITestNGMethod method) {
    m_map.put(result, method);
  }

  public Set<ITestResult> getResults(ITestNGMethod method) {
    Set<ITestResult> result = new HashSet<ITestResult>();
    
    for( ITestResult tr : m_map.keySet()) {
      if (m_map.get(tr).equals(method)) {
        result.add(tr);
      }
    }
    
    return result;
  }

  public Set<ITestResult> getAllResults() {
    return m_map.keySet();
  }

  public int size() {
    return m_map.size();
  }

  public Collection<ITestNGMethod> getAllMethods() {
    return m_map.values();
  }

}
