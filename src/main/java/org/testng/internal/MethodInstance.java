package org.testng.internal;

import org.testng.IMethodInstance;
import org.testng.ITestNGMethod;
import org.testng.collections.Objects;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlTest;

import java.util.Comparator;
import java.util.List;

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
    return Objects.toStringHelper(getClass())
        .add("method", m_method)
        .add("instance", getInstance())
        .toString();
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
      if (class1 == null || class2 == null) {
        if (class1 != null) return -1;
        if (class2 != null) return 1;
        return 0;
      }

      if (! class1.getName().equals(class2.getName())) {
        int index1 = class1.getIndex();
        int index2 = class2.getIndex();
        result = index1 - index2;
      }
      else {
        XmlInclude include1 =
            findXmlInclude(class1.getIncludedMethods(), o1.getMethod().getMethodName());
        XmlInclude include2 =
          findXmlInclude(class2.getIncludedMethods(), o2.getMethod().getMethodName());
        if (include1 != null && include2 != null) {
          result = include1.getIndex() - include2.getIndex();
        }
      }

      return result;
    }

    private XmlInclude findXmlInclude(List<XmlInclude> includedMethods, String methodName) {
      for (XmlInclude xi : includedMethods) {
        if (xi.getName().equals(methodName)) {
          return xi;
        }
      }
      return null;
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
