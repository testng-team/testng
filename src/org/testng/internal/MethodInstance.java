package org.testng.internal;

import org.testng.IMethodInstance;
import org.testng.ITestNGMethod;

import java.util.Comparator;

public class MethodInstance implements IMethodInstance {
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


  public static final Comparator<IMethodInstance> SORT_BY_INDEX 
    = new Comparator<IMethodInstance>() {
    public int compare(IMethodInstance o1, IMethodInstance o2) {
      int index1 = o1.getMethod().getTestClass().getXmlClass().getIndex();
      int index2 = o2.getMethod().getTestClass().getXmlClass().getIndex();
      int result = index1 - index2;

      return result;
    }
  };

//  public static final Comparator<IMethodInstance> SORT_BY_CLASS 
//    = new Comparator<IMethodInstance>() {
//    public int compare(IMethodInstance o1, IMethodInstance o2) {
//      int result= o1.getMethod().getTestClass().getName()
//        .compareTo(o2.getMethod().getTestClass().getName());
//      return result;
//    }
//  };

}
