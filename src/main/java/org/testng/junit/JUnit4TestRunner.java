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
    private Collection<IInvokedMethodListener> m_invokeListeners = Lists.newArrayList();
    private Map<Description, ITestResult> m_findedMethods = new WeakHashMap<>();

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

    public void setInvokedMethodListeners(Collection<IInvokedMethodListener> listeners) {
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
                        if (description.getTestClass() != null) {
                            ITestResult tr = createTestResult(description);
                            m_findedMethods.put(description, tr);
                        }
                        //run everything
                        return true;
                    }
                    for (String m: methods) {
                        Pattern p = Pattern.compile(m);
                        if (p.matcher(description.getMethodName()).matches()) {
                            ITestResult tr = createTestResult(description);
                            m_findedMethods.put(description, tr);
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
            ITestResult tr = m_findedMethods.get(failure.getDescription());
            validate(tr, failure.getDescription());
            runAfterInvocationListeners(tr);
            tr.setStatus(TestResult.SKIP);
            tr.setEndMillis(Calendar.getInstance().getTimeInMillis());
            tr.setThrowable(failure.getException());
            m_parentRunner.addSkippedTest(tr.getMethod(), tr);
            for (ITestListener l : m_listeners) {
                l.onTestSkipped(tr);
            }
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
            ITestResult tr = m_findedMethods.get(failure.getDescription());
            if (tr == null) {
                // Not a test method, should be a config
                tr = createTestResult(failure.getDescription());
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
            }
        }

        @Override
        public void testFinished(Description description) throws Exception {
            ITestResult tr = m_findedMethods.get(description);
            validate(tr, description);
            runAfterInvocationListeners(tr);
            if (!notified.contains(description)) {
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
            if (!notified.contains(description)) {
                notified.add(description);
                ITestResult tr = m_findedMethods.get(description);
                validate(tr, description);
                runAfterInvocationListeners(tr);
                tr.setStatus(TestResult.SKIP);
                tr.setEndMillis(tr.getStartMillis());
                m_parentRunner.addSkippedTest(tr.getMethod(), tr);
                m_methods.add(tr.getMethod());
                for (ITestListener l : m_listeners) {
                    l.onTestSkipped(tr);
                }
            }
        }

        @Override
        public void testRunFinished(Result result) throws Exception {
        }

        @Override
        public void testRunStarted(Description description) throws Exception {

        }

        @Override
        public void testStarted(Description description) throws Exception {
            ITestResult tr = m_findedMethods.get(description);
            validate(tr, description);
            for (ITestListener l : m_listeners) {
                l.onTestStart(tr);
            }
        }

        private void  runAfterInvocationListeners(ITestResult tr) {
            InvokedMethod im = new InvokedMethod(tr.getTestClass(), tr.getMethod(), tr.getEndMillis(), tr);
            for (IInvokedMethodListener l: m_invokeListeners) {
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

        InvokedMethod im = new InvokedMethod(tr.getTestClass(), tr.getMethod(), tr.getStartMillis(), tr);
        m_parentRunner.addInvokedMethod(im);
        for (IInvokedMethodListener l: m_invokeListeners) {
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
        return "org.junit.internal.AssumptionViolatedException".equals(exception.getClass().getCanonicalName());
    }
}
