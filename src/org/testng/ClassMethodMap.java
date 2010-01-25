package org.testng;

import org.testng.collections.Lists;
import org.testng.collections.Maps;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class maintains a map of <CODE><Class, List<ITestNGMethod>></CODE>.
 * It is used by TestWorkers to determine if the method they just ran
 * is the last of its class, in which case it's time to invoke all the
 * afterClass methods.
 * 
 * @author <a href='mailto:the[dot]mindstorm[at]gmail[dot]com'>Alex Popescu</a>
 */
public class ClassMethodMap {
  private Map<Class<?>, List<ITestNGMethod>> m_classMap = Maps.newHashMap();
  // These two variables are used throughout the workers to keep track
  // of what beforeClass/afterClass methods have been invoked
  private Map<ITestClass, Set<Object>> m_beforeClassMethods = Maps.newHashMap();
  private Map<ITestClass, Set<Object>> m_afterClassMethods = Maps.newHashMap();
  
  public ClassMethodMap(ITestNGMethod[] methods) {
    for (ITestNGMethod m : methods) {
      Class<?> c = getMethodClass(m);
      List<ITestNGMethod> l = m_classMap.get(c);
      if (l == null) {
        l = Lists.newArrayList();
        m_classMap.put(c, l);
      }
      l.add(m);
    }
  }
  
  /**
   * Remove the method from this map and returns true if it is the last
   * of its class.
   */
  public synchronized boolean removeAndCheckIfLast(ITestNGMethod m) {
    Class<?> c = getMethodClass(m);
    List<ITestNGMethod> l = m_classMap.get(c);
    l.remove(m);
    return l.size() == 0;
  }

  private Class<?> getMethodClass(ITestNGMethod m) {
    return m.getTestClass().getRealClass();
  }
  
  public Map<ITestClass, Set<Object>> getInvokedBeforeClassMethods() {
    return m_beforeClassMethods;
  }
  
  public Map<ITestClass, Set<Object>> getInvokedAfterClassMethods() {
    return m_afterClassMethods;
  }
  
  public void clear() {
    for(Set<Object> instances: m_beforeClassMethods.values()) {
      instances.clear();
      instances= null;
    }
    for(Set<Object> instances: m_afterClassMethods.values()) {
      instances.clear();
      instances= null;
    }
    m_beforeClassMethods.clear();
    m_afterClassMethods.clear();
  }
}
