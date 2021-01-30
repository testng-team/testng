package org.testng.junit;

import java.lang.reflect.Method;
import junit.framework.Test;
import org.testng.ITestObjectFactory;
import org.testng.internal.ConstructorOrMethod;
import org.testng.internal.Utils;

public class JUnit3TestMethod extends JUnitTestMethod {

  public JUnit3TestMethod(ITestObjectFactory objectFactory, JUnitTestClass owner, Test test) {
    super(objectFactory, owner, getMethod(test), test);
  }

  private static ConstructorOrMethod getMethod(Test t) {
    String name = null;
    try {
      Method nameMethod = t.getClass().getMethod("getName");
      name = (String) nameMethod.invoke(t);
      return new ConstructorOrMethod(t.getClass().getMethod(name));
    } catch (Throwable th) {
      Utils.log(
          "JUnit3TestMethod",
          2,
          "Method '" + name + "' not found in class '" + t + "': " + th.getMessage());
      return null;
    }
  }
}
