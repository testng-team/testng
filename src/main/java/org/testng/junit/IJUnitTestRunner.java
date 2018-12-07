package org.testng.junit;

import java.util.Collection;
import java.util.List;

import org.testng.IInvokedMethodListener;
import org.testng.ITestNGMethod;
import org.testng.TestNGException;
import org.testng.TestRunner;
import org.testng.internal.ClassHelper;
import org.testng.internal.ITestResultNotifier;
import org.testng.internal.Utils;

/**
 * An abstraction interface over JUnit test runners.
 *
 */
public interface IJUnitTestRunner {

  String JUNIT_TESTRUNNER = "org.testng.junit.JUnitTestRunner";
  String JUNIT_4_TESTRUNNER = "org.testng.junit.JUnit4TestRunner";


  void setInvokedMethodListeners(Collection<IInvokedMethodListener> listener);

  void setTestResultNotifier(ITestResultNotifier notifier);

  void run(Class junitTestClass, String... methods);

  List<ITestNGMethod> getTestMethods();

  static IJUnitTestRunner createTestRunner(TestRunner runner) {
    IJUnitTestRunner tr = null;
    try {
      // try to get runner for JUnit 4 first
      Class.forName("org.junit.Test");
      Class<?> clazz = ClassHelper.forName(JUNIT_4_TESTRUNNER);
      if (clazz != null) {
        tr = (IJUnitTestRunner) clazz.newInstance();
        tr.setTestResultNotifier(runner);
      }
    } catch (Throwable t) {
      Utils.log(IJUnitTestRunner.class.getSimpleName(), 2, "JUnit 4 was not found on the classpath");
      try {
        // fallback to JUnit 3
        Class.forName("junit.framework.Test");
        Class<?> clazz = ClassHelper.forName(JUNIT_TESTRUNNER);
        if (clazz != null) {
          tr = (IJUnitTestRunner) clazz.newInstance();
          tr.setTestResultNotifier(runner);
        }
      } catch (Exception ex) {
        Utils.log(IJUnitTestRunner.class.getSimpleName(), 2, "JUnit 3 was not found on the classpath");
        // there's no JUnit on the classpath
        throw new TestNGException("Cannot create JUnit runner", ex);
      }
    }
    return tr;
  }
}
