package org.testng.internal;

import java.util.Comparator;

import org.testng.IMethodInstance;
import org.testng.ITestNGMethod;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlTest;

public class MethodInstance implements IMethodInstance {
  private ITestNGMethod m_method;

  public MethodInstance(ITestNGMethod method) {
    m_method = method;
  }

  @Override
  public ITestNGMethod getMethod() {
    return m_method;
  }

  @Override
  public Object[] getInstances() {
    return new Object[] { getInstance() };
  }

  @Override
  public Object getInstance() {
    return m_method.getInstance();
  }

  @Override
  public String toString() {
    return "[MethodInstance m:" + m_method + " i:" + getInstance();
  }


  public static final Comparator<IMethodInstance> SORT_BY_INDEX
    = new Comparator<IMethodInstance>() {
    @Override
    public int compare(IMethodInstance o1, IMethodInstance o2) {
      // If the two methods are in different <test>
      XmlTest test1 = o1.getMethod().getTestClass().getXmlTest();
      XmlTest test2 = o2.getMethod().getTestClass().getXmlTest();

      // If the two methods are not in the same <test>, we can't compare them
      if (! test1.getName().equals(test2.getName())) {
        return 0;
      }

      int result = 0;

      // If the two methods are in the same <class>, compare them by their method
      // index, otherwise compare them with their class index.
      XmlClass class1 = o1.getMethod().getTestClass().getXmlClass();
      XmlClass class2 = o2.getMethod().getTestClass().getXmlClass();

      // This can happen if these classes came from a @Factory, in which case, they
      // don't have an associated XmlClass
      if (class1 == null || class2 == null) return 0;

      if (! class1.getName().equals(class2.getName())) {
        int index1 = class1.getIndex();
        int index2 = class2.getIndex();
        result = index1 - index2;
      }
      else {
        XmlInclude include1 = o1.getMethod().getXmlInclude();
        XmlInclude include2 = o2.getMethod().getXmlInclude();
        if (include1 != null && include2 != null) {
          result = include1.getIndex() - include2.getIndex();
        }
      }

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
