package org.testng.junit;

import java.util.*;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.TestNGException;
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

    /**
     * A
     * <code>start</code> implementation that ignores the
     * <code>TestResult</code>
     *
     * @param testClass the JUnit test class
     */
    @Override
    public void run(Class testClass) {
        start(testClass);
    }

    /**
     * Starts a test run. Analyzes the command line arguments and runs the given
     * test suite.
     */
    public Result start(Class testCase) {
        try {
            JUnitCore core = new JUnitCore();
            core.addListener(new RL());
            Request r = Request.aClass(testCase);
            return core.run(r);
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
                for (ITestListener l : m_listeners) {
                    l.onTestSuccess(tr);
                }
            }
            m_parentRunner.addInvokedMethod(new InvokedMethod(tr.getTestClass(), tr.getMethod(), new Object[0], true, false, tr.getStartMillis(), tr));
            m_methods.add(tr.getMethod());
        }

        @Override
        public void testIgnored(Description description) throws Exception {
            super.testIgnored(description);
            ITestResult tr = createTestResult(description);
            tr.setStatus(TestResult.SKIP);
            tr.setEndMillis(tr.getStartMillis());
            for (ITestListener l : m_listeners) {
                l.onTestSkipped(tr);
            }
            m_parentRunner.addInvokedMethod(new InvokedMethod(tr.getTestClass(), tr.getMethod(), new Object[0], true, false, tr.getStartMillis(), tr));
            m_methods.add(tr.getMethod());
        }

        @Override
        public void testRunFinished(Result result) throws Exception {
            super.testRunFinished(result);
            //TODO: ITestContext to be implemented by JUnitTestRunner
        }

        @Override
        public void testRunStarted(Description description) throws Exception {
            super.testRunStarted(description);
            //TODO: ITestContext to be implemented by JUnitTestRunner
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
            JUnitUtils.JUnitTestClass tc = new JUnitUtils.JUnitTestClass(test);
            JUnitUtils.JUnitTestMethod tm = new JUnitUtils.JUnitTestMethod(test, tc);

            TestResult tr = new TestResult(tc,
                    test,
                    tm,
                    null,
                    Calendar.getInstance().getTimeInMillis(),
                    0);

            return tr;
        }
    }
}
