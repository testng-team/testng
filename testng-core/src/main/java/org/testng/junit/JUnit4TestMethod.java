package org.testng.junit;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.runner.Description;
import org.testng.ITestObjectFactory;
import org.testng.internal.ConstructorOrMethod;
import org.testng.internal.Utils;
import org.testng.log4testng.Logger;

/**
 * @deprecated - Support for running JUnit tests stands deprecated as of TestNG <code>7.7.0</code>
 */
@Deprecated
public class JUnit4TestMethod extends JUnitTestMethod {

  private static final AtomicBoolean warnOnce = new AtomicBoolean(false);

  public JUnit4TestMethod(
      ITestObjectFactory objectFactory, JUnitTestClass owner, Description desc) {
    super(objectFactory, owner, desc.getMethodName(), getMethod(owner.getRealClass(), desc), desc);
  }

  private static ConstructorOrMethod getMethod(Class<?> c, Description desc) {
    String method = desc.getMethodName();
    if (JUnit4SpockMethod.isSpockClass(c)) {
      if (warnOnce.compareAndSet(false, true)) {
        String msg =
            "Support for running Spock 1.x series is being deprecated and will "
                + "be removed in the upcoming versions of TestNG. Spock 2.x based tests use "
                + "the JUnit5 engine for running them. "
                + "To run both TestNG and Spock2.x tests using JUnit5 refer to "
                + "https://github.com/junit-team/testng-engine";
        Logger.getLogger(JUnit4TestMethod.class).warn(msg);
      }
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
