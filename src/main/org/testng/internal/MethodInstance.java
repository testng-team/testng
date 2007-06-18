package org.testng.internal;

import java.util.Comparator;

import org.testng.ITestNGMethod;

public class MethodInstance {
  private ITestNGMethod m_method;
  private Object[] m_instances;

  public MethodInstance(ITestNGMethod method, Object[] instances) {
    m_method = method;
    m_instances = instances;
  }

  public ITestNGMethod getMethod() {
    return m_method;
  }

  public Object[] getInstances() {
    return m_instances;
  }
  
  public String toString() {
    return "[MethodInstance m:" + m_method + " i:" + m_instances[0];
  }

  public static final Comparator<MethodInstance> SORT_BY_CLASS 
    = new Comparator<MethodInstance>() {
    public int compare(MethodInstance o1, MethodInstance o2) {
      int result= o1.getMethod().getTestClass().getName()
        .compareTo(o2.getMethod().getTestClass().getName());
      if(result == 0) {
        // if they have a single instance try to use it while performing the order 
        Object[] i1= o1.getInstances();
        Object[] i2= o2.getInstances();
        if(i1.length == 1 && i1.length == i2.length) {
            int h1= i1[0].hashCode();
            int h2= i2[0].hashCode();
            if(h1 != h2) {
              return h1 > h2 ? 1 : -1;
            } 
        }
      }

      return result;
    }
  };

}
