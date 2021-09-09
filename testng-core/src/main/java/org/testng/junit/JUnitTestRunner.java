package org.testng.junit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestListener;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.testng.*;
import org.testng.collections.Lists;
import org.testng.internal.ITestResultNotifier;
import org.testng.internal.TestListenerHelper;
import org.testng.internal.invokers.InvokedMethod;

/** A JUnit TestRunner that records/triggers all information/events necessary to TestNG. */
public class JUnitTestRunner implements TestListener, IJUnitTestRunner {
  public static final String SUITE_METHODNAME = "suite";

  private final ITestObjectFactory m_objectFactory;
  private final ITestResultNotifier m_parentRunner;

  private Map<Test, TestRunInfo> m_tests = new WeakHashMap<>();
  private List<ITestNGMethod> m_methods = Lists.newArrayList();
  private Collection<IInvokedMethodListener> m_invokedMethodListeners = Lists.newArrayList();

  public JUnitTestRunner(ITestObjectFactory objectFactory, ITestResultNotifier tr) {
    m_objectFactory = objectFactory;
    m_parentRunner = tr;
  }

  /**
   * Needed from TestRunner in order to figure out what JUnit test methods were run.
   *
   * @return the list of all JUnit test methods run
   */
  @Override
  public List<ITestNGMethod> getTestMethods() {
    return m_methods;
  }

  /** @see junit.framework.TestListener#startTest(junit.framework.Test) */
  @Override
  public void startTest(Test test) {
    m_tests.put(test, new TestRunInfo(Calendar.getInstance().getTimeInMillis()));
  }

  /** @see junit.framework.TestListener#addError(junit.framework.Test, java.lang.Throwable) */
  @Override
  public void addError(Test test, Throwable t) {
    recordFailure(test, t);
  }

  /**
   * @see junit.framework.TestListener#addFailure(junit.framework.Test,
   *     junit.framework.AssertionFailedError)
   */
  @Override
  public void addFailure(Test test, AssertionFailedError t) {
    recordFailure(test, t);
  }

  private void recordFailure(Test test, Throwable t) {
    TestRunInfo tri = m_tests.get(test);
    if (null != tri) {
      tri.setThrowable(t);
    }
  }

  /** @see junit.framework.TestListener#endTest(junit.framework.Test) */
  @Override
  public void endTest(Test test) {
    TestRunInfo tri = m_tests.get(test);
    if (null == tri) {
      return; // HINT: this should never happen. How do I protect myself?
    }

    org.testng.internal.TestResult tr = recordResults(test, tri);
    // For onTestStart method, still run as insert order
    // but regarding
    // onTestSkipped/onTestFailedButWithinSuccessPercentage/onTestFailedWithTimeout/onTestFailure/onTestSuccess, it should be reverse order.
    boolean isFinished = tr.getStatus() != ITestResult.STARTED;
    List<ITestListener> listeners =
        isFinished
            ? Lists.newReversedArrayList(m_parentRunner.getTestListeners())
            : m_parentRunner.getTestListeners();
    TestListenerHelper.runTestListeners(tr, listeners);
  }

  public void setInvokedMethodListeners(Collection<IInvokedMethodListener> listeners) {
    m_invokedMethodListeners = listeners;
  }

  private org.testng.internal.TestResult recordResults(Test test, TestRunInfo tri) {
    JUnitTestClass tc = new JUnit3TestClass(test);
    JUnitTestMethod tm = new JUnit3TestMethod(m_objectFactory, tc, test);

    org.testng.internal.TestResult tr =
        org.testng.internal.TestResult.newEndTimeAwareTestResult(
            tm, null, tri.m_failure, tri.m_start);

    if (tri.isFailure()) {
      tr.setStatus(ITestResult.FAILURE);
      m_parentRunner.addFailedTest(tm, tr);
    } else {
      m_parentRunner.addPassedTest(tm, tr);
    }

    InvokedMethod im = new InvokedMethod(tri.m_start, tr);
    tm.setInvokedAt(im.getDate());
    m_methods.add(tm);
    for (IInvokedMethodListener l : m_invokedMethodListeners) {
      l.beforeInvocation(im, tr);
    }

    return tr;
  }

  /**
   * Returns the Test corresponding to the given suite. This is a template method, subclasses
   * override runFailed(), clearStatus().
   *
   * @param testClass The test class
   * @param methods The test methods
   * @return The corresponding Test
   */
  protected Test getTest(Class<? extends Test> testClass, String... methods) {
    if (methods.length > 0) {
      TestSuite ts = new TestSuite();
      try {
        Constructor<? extends Test> c = testClass.getConstructor(String.class);
        for (String m : methods) {
          try {
            ts.addTest(m_objectFactory.newInstance(c, m));
          } catch (TestNGException ex) {
            runFailed(testClass, ex.getMessage());
          } catch (IllegalArgumentException ex) {
            runFailed(testClass, "actual and formal parameters differ " + ex);
          }
        }
      } catch (NoSuchMethodException ex) {
        runFailed(testClass, "no constructor accepting String argument found " + ex);
      } catch (SecurityException ex) {
        runFailed(testClass, "security exception " + ex);
      }
      return ts;
    }
    Method suiteMethod = null;
    try {
      suiteMethod = testClass.getMethod(SUITE_METHODNAME, new Class[0]);
    } catch (Exception e) {

      // try to extract a test suite automatically
      return new TestSuite(testClass);
    }
    if (!Modifier.isStatic(suiteMethod.getModifiers())) {
      runFailed(testClass, "suite() method must be static");

      return null;
    }
    Test test = null;
    try {
      test = (Test) suiteMethod.invoke(null, (Object[]) new Class[0]); // static method
      if (test == null) {
        return test;
      }
    } catch (InvocationTargetException e) {
      runFailed(testClass, "failed to invoke method suite():" + e.getTargetException().toString());

      return null;
    } catch (IllegalAccessException e) {
      runFailed(testClass, "failed to invoke method suite():" + e.toString());

      return null;
    }

    return test;
  }

  /**
   * A <code>start</code> implementation that ignores the <code>TestResult</code>
   *
   * @param testClass the JUnit test class
   */
  @Override
  public void run(Class testClass, String... methods) {
    start(testClass, methods);
  }

  /**
   * Starts a test run. Analyzes the command line arguments and runs the given test suite.
   *
   * @param testCase The test class to run
   * @param methods The test methods to run
   * @return The test result
   */
  public TestResult start(Class testCase, String... methods) {
    try {
      Test suite = getTest(testCase, methods);

      if (null != suite) {
        return doRun(suite);
      } else {
        runFailed(testCase, "could not create/run JUnit test suite");
      }
    } catch (Exception e) {
      runFailed(testCase, "could not create/run JUnit test suite: " + e.getMessage());
    }

    return null;
  }

  protected void runFailed(Class clazz, String message) {
    throw new TestNGException(
        "Failure in JUnit mode for class " + clazz.getName() + ": " + message);
  }

  /**
   * Creates the TestResult to be used for the test run.
   *
   * @return The created test result
   */
  protected TestResult createTestResult() {
    return new TestResult();
  }

  protected TestResult doRun(Test suite) {
    TestResult result = createTestResult();
    result.addListener(this);
    suite.run(result);

    return result;
  }

  private static class TestRunInfo {
    private final long m_start;
    private Throwable m_failure;

    public TestRunInfo(long start) {
      m_start = start;
    }

    public boolean isFailure() {
      return null != m_failure;
    }

    public void setThrowable(Throwable t) {
      m_failure = t;
    }
  }
}
