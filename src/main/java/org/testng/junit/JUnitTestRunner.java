package org.testng.junit;


import java.lang.reflect.Constructor;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.TestNGException;
import org.testng.collections.Lists;
import org.testng.internal.ITestResultNotifier;
import org.testng.internal.InvokedMethod;

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

/**
 * A JUnit TestRunner that records/triggers all information/events necessary to TestNG.
 *
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class JUnitTestRunner implements TestListener, IJUnitTestRunner {
  public static final String SUITE_METHODNAME = "suite";

  private ITestResultNotifier m_parentRunner;

  private Map<Test, TestRunInfo> m_tests= new WeakHashMap<>();
  private List<ITestNGMethod> m_methods= Lists.newArrayList();
  private Collection<IInvokedMethodListener> m_invokedMethodListeners = Lists.newArrayList();

  public JUnitTestRunner() {
  }

  public JUnitTestRunner(ITestResultNotifier tr) {
    m_parentRunner= tr;
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

  @Override
  public void setTestResultNotifier(ITestResultNotifier notifier) {
    m_parentRunner= notifier;
  }

  /**
   * @see junit.framework.TestListener#startTest(junit.framework.Test)
   */
  @Override
  public void startTest(Test test) {
    m_tests.put(test, new TestRunInfo(Calendar.getInstance().getTimeInMillis()));
  }


  /**
   * @see junit.framework.TestListener#addError(junit.framework.Test, java.lang.Throwable)
   */
  @Override
  public void addError(Test test, Throwable t) {
    recordFailure(test, t);
  }

  /**
   * @see junit.framework.TestListener#addFailure(junit.framework.Test, junit.framework.AssertionFailedError)
   */
  @Override
  public void addFailure(Test test, AssertionFailedError t) {
    recordFailure(test, t);
  }

  private void recordFailure(Test test, Throwable t) {
    TestRunInfo tri= m_tests.get(test);
    if(null != tri) {
      tri.setThrowable(t);
    }
  }

  /**
   * @see junit.framework.TestListener#endTest(junit.framework.Test)
   */
  @Override
  public void endTest(Test test) {
    TestRunInfo tri= m_tests.get(test);
    if(null == tri) {
      return; // HINT: this should never happen. How do I protect myself?
    }

    org.testng.internal.TestResult tr= recordResults(test, tri);

    runTestListeners(tr, m_parentRunner.getTestListeners());
  }

    public void setInvokedMethodListeners(Collection<IInvokedMethodListener> listeners) {
        m_invokedMethodListeners = listeners;
    }


  private org.testng.internal.TestResult recordResults(Test test, TestRunInfo tri)  {
    JUnitTestClass tc= new JUnit3TestClass(test);
    JUnitTestMethod tm= new JUnit3TestMethod(tc, test);

    org.testng.internal.TestResult tr= new org.testng.internal.TestResult(tc,
                                                                          test,
                                                                          tm,
                                                                          tri.m_failure,
                                                                          tri.m_start,
                                                                          Calendar.getInstance().getTimeInMillis(),
                                                                          null);

    if(tri.isFailure()) {
      tr.setStatus(ITestResult.FAILURE);
      m_parentRunner.addFailedTest(tm, tr);
    }
    else {
      m_parentRunner.addPassedTest(tm, tr);
    }

    InvokedMethod im = new InvokedMethod(test, tm, new Object[0], tri.m_start, tr);
    m_parentRunner.addInvokedMethod(im);
    m_methods.add(tm);
    for (IInvokedMethodListener l: m_invokedMethodListeners) {
        l.beforeInvocation(im, tr);
    }

    return tr;
  }

  private static void runTestListeners(ITestResult tr, List<ITestListener> listeners) {
    for (ITestListener itl : listeners) {
      switch(tr.getStatus()) {
        case ITestResult.SKIP: {
          itl.onTestSkipped(tr);
          break;
        }
        case ITestResult.SUCCESS_PERCENTAGE_FAILURE: {
          itl.onTestFailedButWithinSuccessPercentage(tr);
          break;
        }
        case ITestResult.FAILURE: {
          itl.onTestFailure(tr);
          break;
        }
        case ITestResult.SUCCESS: {
          itl.onTestSuccess(tr);
          break;
        }

        case ITestResult.STARTED: {
          itl.onTestStart(tr);
          break;
        }

        default: {
          assert false : "UNKNOWN STATUS:" + tr;
        }
      }
    }
  }

  /**
   * Returns the Test corresponding to the given suite. This is
   * a template method, subclasses override runFailed(), clearStatus().
   */
  protected Test getTest(Class testClass, String... methods) {
    if (methods.length > 0) {
      TestSuite ts = new TestSuite();
      try {
        Constructor c = testClass.getConstructor(String.class);
        for (String m: methods) {
          try {
            ts.addTest((Test) c.newInstance(m));
          } catch (InstantiationException ex) {
            runFailed(testClass, "abstract class " + ex);
          } catch (IllegalAccessException ex) {
            runFailed(testClass, "constructor is not public " + ex);
          } catch (IllegalArgumentException ex) {
            runFailed(testClass, "actual and formal parameters differ " + ex);
          } catch (InvocationTargetException ex) {
            runFailed(testClass, "exception while instatiating test for method '" + m + "' " + ex);
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
    }
    catch (Exception e) {

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
    }
    catch (InvocationTargetException e) {
      runFailed(testClass, "failed to invoke method suite():" + e.getTargetException().toString());

      return null;
    }
    catch (IllegalAccessException e) {
      runFailed(testClass, "failed to invoke method suite():" + e.toString());

      return null;
    }

    return test;
  }

  /**
   * A <code>start</code> implementation that ignores the <code>TestResult</code>
   * @param testClass the JUnit test class
   */
  @Override
  public void run(Class testClass, String... methods) {
    start(testClass, methods);
  }

  /**
   * Starts a test run. Analyzes the command line arguments and runs the given
   * test suite.
   */
  public TestResult start(Class testCase, String... methods) {
    try {
      Test suite = getTest(testCase, methods);

      if(null != suite) {
        return doRun(suite);
      }
      else {
        runFailed(testCase, "could not create/run JUnit test suite");
      }
    }
    catch (Exception e) {
      runFailed(testCase, "could not create/run JUnit test suite: " + e.getMessage());
    }

    return null;
  }

  protected void runFailed(Class clazz, String message) {
    throw new TestNGException("Failure in JUnit mode for class " + clazz.getName() + ": " + message);
  }

  /**
   * Creates the TestResult to be used for the test run.
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
      m_start= start;
    }

    public boolean isFailure() {
      return null != m_failure;
    }

    public void setThrowable(Throwable t) {
      m_failure= t;
    }
  }
}
