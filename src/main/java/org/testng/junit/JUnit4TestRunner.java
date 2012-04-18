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
import org.testng.internal.InvokedMethod;
import org.testng.internal.TestResult;

/**
 * A JUnit TestRunner that records/triggers all information/events necessary to
 * TestNG.
 *
 * @author Lukas Jungmann
 */
public class JUnit4TestRunner implements IJUnitTestRunner {

    private ITestResultNotifier m_parentRunner;
    private List<ITestNGMethod> m_methods = Lists.newArrayList();
    private List<ITestListener> m_listeners = Lists.newArrayList();
    private List<IInvokedMethodListener> m_invokeListeners = Lists.newArrayList();

    public JUnit4TestRunner() {
    }

    public JUnit4TestRunner(ITestResultNotifier tr) {
        m_parentRunner = tr;
        m_listeners = m_parentRunner.getTestListeners();
    }

    /**
     * Needed from TestRunner in order to figure out what JUnit test methods
     * were run.
     *
     * @return the list of all JUnit test methods run
     */
    @Override
    public List<ITestNGMethod> getTestMethods() {
        return m_methods;
    }

    @Override
    public void setTestResultNotifier(ITestResultNotifier notifier) {
        m_parentRunner = notifier;
        m_listeners = m_parentRunner.getTestListeners();
    }

    public void setInvokedMethodListeners(List<IInvokedMethodListener> listeners) {
        m_invokeListeners = listeners;
    }

    /**
     * A
     * <code>start</code> implementation that ignores the
     * <code>TestResult</code>
     *
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
    public Result start(final Class testCase, final String... methods) {
        try {
            JUnitCore core = new JUnitCore();
            core.addListener(new RL());
            Request r = Request.aClass(testCase);
            return core.run(r.filterWith(new Filter() {

                @Override
                public boolean shouldRun(Description description) {
                    if (description == null) {
                        return false;
                    }
                    if (methods.length == 0) {
                        //run everything
                        return true;
                    }
                    for (String m: methods) {
                        Pattern p = Pattern.compile(m);
                        return p.matcher(description.getMethodName()).matches();
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

        private Map<Description, ITestResult> runs = new WeakHashMap<Description, ITestResult>();
        private List<Description> failures = new LinkedList<Description>();

        @Override
        public void testAssumptionFailure(Failure failure) {
            super.testAssumptionFailure(failure);
            ITestResult tr = runs.get(failure.getDescription());
            tr.setStatus(TestResult.FAILURE);
            tr.setEndMillis(Calendar.getInstance().getTimeInMillis());
            tr.setThrowable(failure.getException());
            m_parentRunner.addFailedTest(tr.getMethod(), tr);
            for (ITestListener l : m_listeners) {
                l.onTestFailure(tr);
            }
        }

        @Override
        public void testFailure(Failure failure) throws Exception {
            super.testFailure(failure);
            failures.add(failure.getDescription());
            ITestResult tr = runs.get(failure.getDescription());
            tr.setStatus(TestResult.FAILURE);
            tr.setEndMillis(Calendar.getInstance().getTimeInMillis());
            tr.setThrowable(failure.getException());
            m_parentRunner.addFailedTest(tr.getMethod(), tr);
            for (ITestListener l : m_listeners) {
                l.onTestFailure(tr);
            }
        }

        @Override
        public void testFinished(Description description) throws Exception {
            super.testFinished(description);
            ITestResult tr = runs.get(description);
            if (!failures.contains(description)) {
                tr.setStatus(TestResult.SUCCESS);
                tr.setEndMillis(Calendar.getInstance().getTimeInMillis());
                m_parentRunner.addPassedTest(tr.getMethod(), tr);
                for (ITestListener l : m_listeners) {
                    l.onTestSuccess(tr);
                }
            }
            m_methods.add(tr.getMethod());
        }

        @Override
        public void testIgnored(Description description) throws Exception {
            super.testIgnored(description);
            ITestResult tr = createTestResult(description);
            tr.setStatus(TestResult.SKIP);
            tr.setEndMillis(tr.getStartMillis());
            m_parentRunner.addSkippedTest(tr.getMethod(), tr);
            m_methods.add(tr.getMethod());
            for (ITestListener l : m_listeners) {
                l.onTestSkipped(tr);
            }
        }

        @Override
        public void testRunFinished(Result result) throws Exception {
            super.testRunFinished(result);
        }

        @Override
        public void testRunStarted(Description description) throws Exception {
            super.testRunStarted(description);
        }

        @Override
        public void testStarted(Description description) throws Exception {
            super.testStarted(description);
            ITestResult tr = createTestResult(description);
            runs.put(description, tr);
            for (ITestListener l : m_listeners) {
                l.onTestStart(tr);
            }
        }

        private ITestResult createTestResult(Description test) {
            JUnit4TestClass tc = new JUnit4TestClass(test);
            JUnitTestMethod tm = new JUnit4TestMethod(tc, test);

            TestResult tr = new TestResult(tc,
                    test,
                    tm,
                    null,
                    Calendar.getInstance().getTimeInMillis(),
                    0,
                    null);

            InvokedMethod im = new InvokedMethod(tr.getTestClass(), tr.getMethod(), new Object[0], true, false, tr.getStartMillis(), tr);
            m_parentRunner.addInvokedMethod(im);
            for (IInvokedMethodListener l: m_invokeListeners) {
                l.beforeInvocation(im, tr);
            }
            return tr;
        }
    }
}
