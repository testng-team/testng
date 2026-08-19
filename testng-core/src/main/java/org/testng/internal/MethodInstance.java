package org.testng.internal;

import java.util.Comparator;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.testng.IMethodInstance;
import org.testng.ITestNGMethod;
import org.testng.collections.Objects;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlTest;

public class MethodInstance implements IMethodInstance {
  private final ITestNGMethod m_method;

  public MethodInstance(ITestNGMethod method) {
    m_method = method;
  }

  @Override
  public ITestNGMethod getMethod() {
    return m_method;
  }

  @Override
  public @Nullable Object getInstance() {
    return m_method.getInstance();
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(getClass())
        .add("method", m_method)
        .add("instance", getInstance())
        .toString();
  }

  public static final Comparator<IMethodInstance> SORT_BY_INDEX =
      new Comparator<IMethodInstance>() {
        @Override
        public int compare(IMethodInstance o1, IMethodInstance o2) {
          // If the two methods are in different <test>
          XmlTest test1 = Utils.requireTestClassOf(o1.getMethod()).getXmlTest();
          XmlTest test2 = Utils.requireTestClassOf(o2.getMethod()).getXmlTest();

          // If the two methods are not in the same <test>, we can't compare them. A method a
          // @Factory produced has no <test> tag of its own, which reads the same way here.
          String testName1 = test1 == null ? null : test1.getName();
          String testName2 = test2 == null ? null : test2.getName();
          if (!java.util.Objects.equals(testName1, testName2)) {
            return 0;
          }

          int result = 0;

          // If the two methods are in the same <class>, compare them by their method
          // index, otherwise compare them with their class index.
          XmlClass class1 = Utils.requireTestClassOf(o1.getMethod()).getXmlClass();
          XmlClass class2 = Utils.requireTestClassOf(o2.getMethod()).getXmlClass();

          // This can happen if these classes came from a @Factory, in which case, they
          // don't have an associated XmlClass
          if (class1 == null || class2 == null) {
            if (class1 != null) {
              return -1;
            }
            if (class2 != null) {
              return 1;
            }
            return 0;
          }

          if (!class1.getName().equals(class2.getName())) {
            int index1 = class1.getIndex();
            int index2 = class2.getIndex();
            result = index1 - index2;
          } else {
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

        private @Nullable XmlInclude findXmlInclude(
            List<XmlInclude> includedMethods, String methodName) {
          for (XmlInclude xi : includedMethods) {
            if (xi.getName().equals(methodName)) {
              return xi;
            }
          }
          return null;
        }
      };
}
