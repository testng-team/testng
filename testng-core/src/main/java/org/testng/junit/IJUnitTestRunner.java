package org.testng.junit;

import java.util.Collection;
import java.util.List;
import org.testng.IInvokedMethodListener;
import org.testng.ITestNGMethod;
import org.testng.ITestObjectFactory;
import org.testng.TestNGException;
import org.testng.internal.ITestResultNotifier;
import org.testng.internal.Utils;

/** An abstraction interface over JUnit test runners. */
public interface IJUnitTestRunner {

  void setInvokedMethodListeners(Collection<IInvokedMethodListener> listener);

  @Deprecated
  default void setTestResultNotifier(ITestResultNotifier notifier) {}

  void run(Class<?> junitTestClass, String... methods);

  List<ITestNGMethod> getTestMethods();

  static IJUnitTestRunner createTestRunner(
      ITestObjectFactory objectFactory, ITestResultNotifier runner) {
    IJUnitTestRunner tr;
    try {
      // try to get runner for JUnit 4 first
      Class.forName("org.junit.Test");
      tr = new JUnit4TestRunner(objectFactory, runner);
    } catch (Throwable t) {
      Utils.log(
          IJUnitTestRunner.class.getSimpleName(), 2, "JUnit 4 was not found on the classpath");
      try {
        // fallback to JUnit 3
        Class.forName("junit.framework.Test");
        tr = new JUnitTestRunner(objectFactory, runner);
      } catch (Exception ex) {
        Utils.log(
            IJUnitTestRunner.class.getSimpleName(), 2, "JUnit 3 was not found on the classpath");
        // there's no JUnit on the classpath
        throw new TestNGException("Cannot create JUnit runner", ex);
      }
    }
    return tr;
  }
}
