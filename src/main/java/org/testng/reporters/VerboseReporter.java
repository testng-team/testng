/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.testng.reporters;

import java.lang.reflect.Method;
import java.util.List;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.internal.Utils;

/**
 *
 * @author Lukas Jungmann
 */
//topic for discussion with Cedric
//would be better to merge this class and org.testng.reporters.TextReporter
//or keep this as separate listener?
//main diferences are:
// - format of the message being logged
// - message is logged immediately instead of in onFinish event
// - all messages have specific prefix
public class VerboseReporter extends TestListenerAdapter {

    public static final String LISTENER_PREFIX = "[VerboseTestNG] ";
    private String testName;
    private final String prefix;

    public VerboseReporter() {
        this(LISTENER_PREFIX);
    }

    public VerboseReporter(String prefix) {
       this.prefix = prefix;
    }

    //see https://github.com/cbeust/testng/issues/124
    private ITestResult r = null;

    @Override
    public void beforeConfiguration(ITestResult tr) {
        if (tr.equals(r)) {
            r = null;
            return;
        }
        r = tr;
        super.beforeConfiguration(tr);
        logResult("INVOKING CONFIGURATION", detailedMethodName(tr.getMethod(), true));
    }

    @Override
    public void onConfigurationFailure(ITestResult tr) {
        if (tr.equals(r)) {
            r = null;
            return;
        }
        r = tr;
        super.onConfigurationFailure(tr);
        Throwable ex = tr.getThrowable();
        String stackTrace = "";
        if (ex != null) {
            stackTrace = Utils.stackTrace(ex, false)[0];
        }
        long duration = tr.getEndMillis() - tr.getStartMillis();
        logResult("FAILED CONFIGURATION", detailedMethodName(tr.getMethod(), true), tr.getMethod().getDescription(), stackTrace, tr.getParameters(), tr.getMethod().getMethod().getParameterTypes(), duration);
    }

    @Override
    public void onConfigurationSkip(ITestResult tr) {
        if (tr.equals(r)) {
            r = null;
            return;
        }
        r = tr;
        super.onConfigurationSkip(tr);
        long duration = tr.getEndMillis() - tr.getStartMillis();
        logResult("SKIPPED CONFIGURATION", detailedMethodName(tr.getMethod(), true), tr.getMethod().getDescription(), null, tr.getParameters(), tr.getMethod().getMethod().getParameterTypes(), duration);
    }

    @Override
    public void onConfigurationSuccess(ITestResult tr) {
        if (tr.equals(r)) {
            r = null;
            return;
        }
        r = tr;
        super.onConfigurationSuccess(tr);
        long duration = tr.getEndMillis() - tr.getStartMillis();
        logResult("PASSED CONFIGURATION", detailedMethodName(tr.getMethod(), true), tr.getMethod().getDescription(), null, tr.getParameters(), tr.getMethod().getMethod().getParameterTypes(), duration);
    }

    @Override
    public void onTestFailure(ITestResult tr) {
        super.onTestFailure(tr);
        Throwable ex = tr.getThrowable();
        String stackTrace = "";
        if (ex != null) {
            stackTrace = Utils.stackTrace(ex, false)[0];
        }
        logResult("FAILED", tr, stackTrace);
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult tr) {
        super.onTestFailedButWithinSuccessPercentage(tr);
        logResult("PASSED with failures", tr, null);
    }

    @Override
    public void onTestSkipped(ITestResult tr) {
        super.onTestSkipped(tr);
        Throwable throwable = tr.getThrowable();
        logResult("SKIPPED", tr, throwable != null ? Utils.stackTrace(throwable, false)[0] : null);
    }

    @Override
    public void onTestSuccess(ITestResult tr) {
        super.onTestSuccess(tr);
        logResult("PASSED", tr, null);
    }

    @Override
    public void onStart(ITestContext ctx) {
        testName = ctx.getName();//ctx.getSuite().getXmlSuite().getFileName();
        logResult("RUNNING", "Suite: \"" + testName + "\" containing \"" + ctx.getAllTestMethods().length + "\" Tests (config: " + ctx.getSuite().getXmlSuite().getFileName() + ")");

    }

    @Override
    public void onFinish(ITestContext context) {
        logResults();
        testName = null;
    }

    @Override
    public void onTestStart(ITestResult tr) {
        logResult("INVOKING", detailedMethodName(tr.getMethod(), true));
    }

    private ITestNGMethod[] resultsToMethods(List<ITestResult> results) {
        ITestNGMethod[] result = new ITestNGMethod[results.size()];
        int i = 0;
        for (ITestResult tr : results) {
            result[i++] = tr.getMethod();
        }
        return result;
    }

    private void logResults() {
        //
        // Log test summary
        //
        ITestNGMethod[] ft = resultsToMethods(getFailedTests());
        StringBuffer logBuf = new StringBuffer("\n===============================================\n");
        logBuf.append("    ").append(getName()).append("\n");
        logBuf.append("    Tests run: ").append(Utils.calculateInvokedMethodCount(getAllTestMethods())).append(", Failures: ").append(Utils.calculateInvokedMethodCount(ft)).append(", Skips: ").append(Utils.calculateInvokedMethodCount(resultsToMethods(getSkippedTests())));
        int confFailures = getConfigurationFailures().size();
        int confSkips = getConfigurationSkips().size();
        if (confFailures > 0 || confSkips > 0) {
            logBuf.append("\n").append("    Configuration Failures: ").append(confFailures).append(", Skips: ").append(confSkips);
        }
        logBuf.append("\n===============================================");
        logResult("", logBuf.toString());
    }

    private String getName() {
        return testName;
    }

    private void logResult(String status, ITestResult tr, String stackTrace) {
        long duration = tr.getEndMillis() - tr.getStartMillis();
        logResult(status, detailedMethodName(tr.getMethod(), true), tr.getMethod().getDescription(), stackTrace, tr.getParameters(), tr.getMethod().getMethod().getParameterTypes(), duration);
    }

    private void logResult(String status, String message) {
        StringBuffer buf = new StringBuffer();
        if (Utils.isStringNotBlank(status)) {
            buf.append(status).append(": ");
        }
        buf.append(message);
//        System.out.println("LOG: " + buf.toString());
        //prefix all output lines
        System.out.println(buf.toString().replaceAll("(?m)^", prefix));
    }

    private void logResult(String status, String name, String description, String stackTrace, Object[] params, Class[] paramTypes, long duration) {
        StringBuffer msg = new StringBuffer(name);
        if (null != params && params.length > 0) {
            msg.append("(value(s): ");
            // The error might be a data provider parameter mismatch, so make
            // a special case here
            if (params.length != paramTypes.length) {
                msg.append(name + ": Wrong number of arguments were passed by " + "the Data Provider: found " + params.length + " but " + "expected " + paramTypes.length + ")");
            } else {
                for (int i = 0; i < params.length; i++) {
                    if (i > 0) {
                        msg.append(", ");
                    }
                    msg.append(Utils.toString(params[i], paramTypes[i]));
                }
                msg.append(")");
            }
        }
        msg.append(" finished in ");
        msg.append(duration);
        msg.append(" ms");
        if (!Utils.isStringEmpty(description)) {
            msg.append("\n");
            for (int i = 0; i < status.length() + 2; i++) {
                msg.append(" ");
            }
            msg.append(description);
        }
        if (!Utils.isStringEmpty(stackTrace)) {
            msg.append("\n").append(stackTrace.substring(0, stackTrace.lastIndexOf(System.getProperty("line.separator"))));
        }
        logResult(status, msg.toString());
    }

    @Deprecated
    //perhaps should rather to adopt the original method
    private String detailedMethodName(ITestNGMethod method, boolean fqn) {
        Method m = method.getMethod();
        StringBuffer buf = new StringBuffer();
        buf.append("\"");
        if (getName() != null) {
            buf.append(getName());
        } else {
            buf.append("UNKNOWN");
        }
        buf.append("\"");
        buf.append(" - ");
        if (method.isBeforeSuiteConfiguration()) {
            buf.append("@BeforeSuite ");
        } else if (method.isBeforeTestConfiguration()) {
            buf.append("@BeforeTest ");
        } else if (method.isBeforeClassConfiguration()) {
            buf.append("@BeforeClass ");
        } else if (method.isBeforeGroupsConfiguration()) {
            buf.append("@BeforeGroups ");
        } else if (method.isBeforeMethodConfiguration()) {
            buf.append("@BeforeMethod ");
        } else if (method.isAfterMethodConfiguration()) {
            buf.append("@AfterMethod ");
        } else if (method.isAfterGroupsConfiguration()) {
            buf.append("@AfterGroups ");
        } else if (method.isAfterClassConfiguration()) {
            buf.append("@AfterClass ");
        } else if (method.isAfterTestConfiguration()) {
            buf.append("@AfterTest ");
        } else if (method.isAfterSuiteConfiguration()) {
            buf.append("@AfterSuite ");
        }
        buf.append(m.getDeclaringClass().getName());
        buf.append(".");
        buf.append(m.getName());
        buf.append("(");
        int i = 0;
        for (Class<?> p : m.getParameterTypes()) {
            if (i++ > 0) {
                buf.append(", ");
            }
            buf.append(p.getName());
        }
        buf.append(")");
        return buf.toString();//buf.append(fqn ? method.toString() : method.getMethodName()).toString();
    }

    @Override
    public String toString() {
        return "VerboseReporter{" + "testName=" + testName + ", r=" + r + '}';
    }

}
