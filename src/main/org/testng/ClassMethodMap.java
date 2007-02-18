package org.testng;

import java.util.ArrayList;
import java.util.HashMap;
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
  private Map<Class, List<ITestNGMethod>> m_classMap = new HashMap<Class, List<ITestNGMethod>>();
  // These two variables are used throughout the workers to keep track
  // of what beforeClass/afterClass methods have been invoked
  private Map<ITestClass, Set<Object>> m_beforeClassMethods = new HashMap<ITestClass, Set<Object>>();
  private Map<ITestClass, Set<Object>> m_afterClassMethods = new HashMap<ITestClass, Set<Object>>();
  

  
  public ClassMethodMap(ITestNGMethod[] methods) {
    for (ITestNGMethod m : methods) {
      Class c = getMethodClass(m);
      List<ITestNGMethod> l = m_classMap.get(c);
      if (l == null) {
        l = new ArrayList<ITestNGMethod>();
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
    Class c = getMethodClass(m);
    List<ITestNGMethod> l = m_classMap.get(c);
    l.remove(m);
    return l.size() == 0;
  }

  private Class getMethodClass(ITestNGMethod m) {
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
