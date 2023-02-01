package org.testng.junit;

import java.util.*;
import java.util.regex.Pattern;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.testng.*;
import org.testng.collections.Lists;
import org.testng.internal.ITestResultNotifier;
import org.testng.internal.TestResult;
import org.testng.internal.invokers.IInvocationStatus;
import org.testng.internal.invokers.InvokedMethod;

/**
 * A JUnit TestRunner that records/triggers all information/events necessary to TestNG.
 *
 * @deprecated - Support for running JUnit tests stands deprecated as of TestNG <code>7.7.0</code>
 */
@Deprecated
public class JUnit4TestRunner implements IJUnitTestRunner {

  private final ITestObjectFactory objectFactory;
  private final ITestResultNotifier m_parentRunner;
  private final List<ITestListener> m_listeners;
  private final List<ITestNGMethod> m_methods = Lists.newArrayList();
  private Collection<IInvokedMethodListener> m_invokeListeners = Lists.newArrayList();
  private final Map<Description, ITestResult> m_foundMethods = new WeakHashMap<>();
  private final ITestListener m_exitCodeListener;

  public JUnit4TestRunner(ITestObjectFactory objectFactory, ITestResultNotifier tr) {
    this.objectFactory = objectFactory;
    m_parentRunner = tr;
    m_listeners = m_parentRunner.getTestListeners();
    m_exitCodeListener = m_parentRunner.getExitCodeListener();
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

  public void setInvokedMethodListeners(Collection<IInvokedMethodListener> listeners) {
    m_invokeListeners = listeners;
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
   * @param testCase The test class
   * @param methods The test methods
   * @return The result
   */
  public Result start(final Class testCase, final String... methods) {
    try {
      JUnitCore core = new JUnitCore();
      core.addListener(new RL());
      Request r = Request.aClass(testCase);
      return core.run(
          r.filterWith(
              new Filter() {

                @Override
                public boolean shouldRun(Description description) {
                  if (description == null) {
                    return false;
                  }
                  if (methods.length == 0) {
                    if (description.getTestClass() != null) {
                      ITestResult tr = createTestResult(objectFactory, description);
                      m_foundMethods.put(description, tr);
                    }
                    // run everything
                    return true;
                  }
                  for (String m : methods) {
                    Pattern p = Pattern.compile(m);
                    if (p.matcher(description.getMethodName()).matches()) {
                      ITestResult tr = createTestResult(objectFactory, description);
                      m_foundMethods.put(description, tr);
                      return true;
                    }
                  }
                  return false;
                }

                @Override
                public String describe() {
                  return "TestNG method filter";
                }
              }));
    } catch (Throwable t) {
      throw new TestNGException("Failure in JUnit mode for class " + testCase.getName(), t);
    }
  }

  private class RL extends RunListener {

    private List<Description> notified = new LinkedList<>();

    @Override
    public void testAssumptionFailure(Failure failure) {
      notified.add(failure.getDescription());
      ITestResult tr = m_foundMethods.get(failure.getDescription());
      validate(tr, failure.getDescription());
      runAfterInvocationListeners(tr);
      tr.setStatus(TestResult.SKIP);
      tr.setEndMillis(Calendar.getInstance().getTimeInMillis());
      tr.setThrowable(failure.getException());
      m_parentRunner.addSkippedTest(tr.getMethod(), tr);
      for (ITestListener l : m_listeners) {
        l.onTestSkipped(tr);
      }
      m_exitCodeListener.onTestSkipped(tr);
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
      if (failure == null) {
        return;
      }
      if (isAssumptionFailed(failure)) {
        this.testAssumptionFailure(failure);
        return;
      }
      notified.add(failure.getDescription());
      ITestResult tr = m_foundMethods.get(failure.getDescription());
      if (tr == null) {
        // Not a test method, should be a config
        tr = createTestResult(objectFactory, failure.getDescription());
        runAfterInvocationListeners(tr);
        tr.setStatus(TestResult.FAILURE);
        tr.setEndMillis(Calendar.getInstance().getTimeInMillis());
        tr.setThrowable(failure.getException());
        for (IConfigurationListener l : m_parentRunner.getConfigurationListeners()) {
          l.onConfigurationFailure(tr);
        }
        for (Description childDesc : failure.getDescription().getChildren()) {
          testIgnored(childDesc);
        }
      } else {
        runAfterInvocationListeners(tr);
        tr.setStatus(TestResult.FAILURE);
        tr.setEndMillis(Calendar.getInstance().getTimeInMillis());
        tr.setThrowable(failure.getException());
        m_parentRunner.addFailedTest(tr.getMethod(), tr);
        for (ITestListener l : m_listeners) {
          l.onTestFailure(tr);
        }
        m_exitCodeListener.onTestFailure(tr);
      }
    }

    @Override
    public void testFinished(Description description) throws Exception {
      ITestResult tr = m_foundMethods.get(description);
      validate(tr, description);
      runAfterInvocationListeners(tr);
      if (!notified.contains(description)) {
        tr.setStatus(TestResult.SUCCESS);
        tr.setEndMillis(Calendar.getInstance().getTimeInMillis());
        m_parentRunner.addPassedTest(tr.getMethod(), tr);
        for (ITestListener l : m_listeners) {
          l.onTestSuccess(tr);
        }
        m_exitCodeListener.onTestSuccess(tr);
      }
      m_methods.add(tr.getMethod());
    }

    @Override
    public void testIgnored(Description description) throws Exception {
      if (!notified.contains(description)) {
        notified.add(description);
        ITestResult tr = m_foundMethods.get(description);
        validate(tr, description);
        runAfterInvocationListeners(tr);
        tr.setStatus(TestResult.SKIP);
        tr.setEndMillis(tr.getStartMillis());
        m_parentRunner.addSkippedTest(tr.getMethod(), tr);
        m_methods.add(tr.getMethod());
        for (ITestListener l : m_listeners) {
          l.onTestSkipped(tr);
        }
        m_exitCodeListener.onTestSkipped(tr);
      }
    }

    @Override
    public void testRunFinished(Result result) throws Exception {}

    @Override
    public void testRunStarted(Description description) throws Exception {}

    @Override
    public void testStarted(Description description) throws Exception {
      ITestResult tr = m_foundMethods.get(description);
      validate(tr, description);
      for (ITestListener l : m_listeners) {
        l.onTestStart(tr);
      }
      m_exitCodeListener.onTestStart(tr);
    }

    private void runAfterInvocationListeners(ITestResult tr) {
      InvokedMethod im = new InvokedMethod(tr.getEndMillis(), tr);
      for (IInvokedMethodListener l : m_invokeListeners) {
        l.afterInvocation(im, tr);
      }
    }

    private void validate(ITestResult tr, Description description) {
      if (tr == null) {
        throw new TestNGException(stringify(description));
      }
    }

    private String stringify(Description description) {
      return description.getClassName() + "." + description.getMethodName() + "()";
    }
  }

  private ITestResult createTestResult(ITestObjectFactory objectFactory, Description test) {
    JUnit4TestClass tc = new JUnit4TestClass(test);
    JUnitTestMethod tm = new JUnit4TestMethod(objectFactory, tc, test);

    ITestContext ctx = null;
    if (m_parentRunner instanceof ITestContext) {
      ctx = (ITestContext) m_parentRunner;
    }
    TestResult tr = TestResult.newContextAwareTestResult(tm, ctx);

    InvokedMethod im = new InvokedMethod(tr.getStartMillis(), tr);
    if (tr.getMethod() instanceof IInvocationStatus) {
      ((IInvocationStatus) tr.getMethod()).setInvokedAt(im.getDate());
    }
    for (IInvokedMethodListener l : m_invokeListeners) {
      l.beforeInvocation(im, tr);
    }
    return tr;
  }

  private static boolean isAssumptionFailed(Failure failure) {
    //noinspection ThrowableResultOfMethodCallIgnored
    final Throwable exception = failure.getException();
    //noinspection SimplifiableIfStatement
    if (exception == null) {
      return false;
    }
    return "org.junit.internal.AssumptionViolatedException"
        .equals(exception.getClass().getCanonicalName());
  }
}
