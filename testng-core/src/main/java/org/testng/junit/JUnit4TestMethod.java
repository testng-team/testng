package org.testng.junit;

import org.junit.runner.Description;
import org.testng.ITestObjectFactory;
import org.testng.internal.ConstructorOrMethod;
import org.testng.internal.Utils;

public class JUnit4TestMethod extends JUnitTestMethod {

  public JUnit4TestMethod(
      ITestObjectFactory objectFactory, JUnitTestClass owner, Description desc) {
    super(objectFactory, owner, desc.getMethodName(), getMethod(owner.getRealClass(), desc), desc);
  }

  private static ConstructorOrMethod getMethod(Class<?> c, Description desc) {
    String method = desc.getMethodName();
    if (JUnit4SpockMethod.isSpockClass(c)) {
      return new JUnit4SpockMethod(desc);
    }
    if (method == null) {
      return new JUnit4ConfigurationMethod(c);
    }
    // remove [index] from method name in case of parameterized test
    int idx = method.indexOf('[');
    if (idx != -1) {
      method = method.substring(0, idx);
    }
    try {
      return new ConstructorOrMethod(c.getMethod(method));
    } catch (Throwable t) {
      Utils.log(
          "JUnit4TestMethod",
          2,
          "Method '" + method + "' not found in class '" + c.getName() + "': " + t.getMessage());
      return null;
    }
  }

  @Override
  public boolean isTest() {
    return !(m_method instanceof JUnit4ConfigurationMethod);
  }

  @Override
  public String toString() {
    return m_method.toString();
  }
}
