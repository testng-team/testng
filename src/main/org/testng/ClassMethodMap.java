package org.testng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class maintains a map of <Class, List<methods>>.
 * It is used by TestWorkers to determine if the method they just ran
 * is the last of its class, in which case it's time to invoke all the
 * afterClass methods
 */
public class ClassMethodMap {
  private Map<Class, List<ITestNGMethod>> m_classMap = new HashMap<Class, List<ITestNGMethod>>();
  
  public ClassMethodMap(ITestNGMethod[] methods) {
    this(methods, false);
  }
  
  public ClassMethodMap(ITestNGMethod[] methods, boolean expandOnInvocationCount) {    
    for (ITestNGMethod m : methods) {
      Class c = getMethodClass(m);
      List<ITestNGMethod> l = m_classMap.get(c);
      if (l == null) {
        l = new ArrayList<ITestNGMethod>();
        m_classMap.put(c, l);
      }
      l.add(m);
      if(expandOnInvocationCount && m.getInvocationCount() > 1) {
        for(int i= 1; i < m.getInvocationCount(); i++) {
          l.add(m);
        }
      }
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
  
  
}
