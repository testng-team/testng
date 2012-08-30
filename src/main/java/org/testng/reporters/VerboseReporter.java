package org.testng.reporters;

import java.lang.reflect.Method;
import java.util.List;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.internal.Utils;

/**
 * Reporter printing out detailed messages about what TestNG
 * is going to run and what is the status of what has been just run.
 *
 * To see messages from this reporter, either run Ant in verbose mode ('ant -v')
 * or set verbose level to 5 or higher
 *
 * @author Lukas Jungmann
 * @since 6.4
 */
public class VerboseReporter extends TestListenerAdapter {

    /**
     * Default prefix for messages printed out by this reporter
     *
     */
    public static final String LISTENER_PREFIX = "[VerboseTestNG] ";
    private String suiteName;
    private final String prefix;

    private enum Status {

        SUCCESS(ITestResult.SUCCESS), FAILURE(ITestResult.FAILURE), SKIP(ITestResult.SKIP),
        SUCCESS_PERCENTAGE_FAILURE(ITestResult.SUCCESS_PERCENTAGE_FAILURE), STARTED(ITestResult.STARTED);
        private int status;

        private Status(int i) {
            status = i;
        }
    }

    /**
     * Default constructor
     */
    public VerboseReporter() {
        this(LISTENER_PREFIX);
    }

    /**
     * Create VerboseReporter with custom prefix
     *
     * @param prefix prefix for messages printed out by this reporter
     */
    public VerboseReporter(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void beforeConfiguration(ITestResult tr) {
        super.beforeConfiguration(tr);
        logTestResult(Status.STARTED, tr, true);
    }

    @Override
    public void onConfigurationFailure(ITestResult tr) {
        super.onConfigurationFailure(tr);
        logTestResult(Status.FAILURE, tr, true);
    }

    @Override
    public void onConfigurationSkip(ITestResult tr) {
        super.onConfigurationSkip(tr);
        logTestResult(Status.SKIP, tr, true);
    }

    @Override
    public void onConfigurationSuccess(ITestResult tr) {
        super.onConfigurationSuccess(tr);
        logTestResult(Status.SUCCESS, tr, true);
    }

    @Override
    public void onTestStart(ITestResult tr) {
        logTestResult(Status.STARTED, tr, false);
    }

    @Override
    public void onTestFailure(ITestResult tr) {
        super.onTestFailure(tr);
        logTestResult(Status.FAILURE, tr, false);
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult tr) {
        super.onTestFailedButWithinSuccessPercentage(tr);
        logTestResult(Status.SUCCESS_PERCENTAGE_FAILURE, tr, false);
    }

    @Override
    public void onTestSkipped(ITestResult tr) {
        super.onTestSkipped(tr);
        logTestResult(Status.SKIP, tr, false);
    }

    @Override
    public void onTestSuccess(ITestResult tr) {
        super.onTestSuccess(tr);
        logTestResult(Status.SUCCESS, tr, false);
    }

    @Override
    public void onStart(ITestContext ctx) {
        suiteName = ctx.getName();//ctx.getSuite().getXmlSuite().getFileName();
        log("RUNNING: Suite: \"" + suiteName + "\" containing \"" + ctx.getAllTestMethods().length + "\" Tests (config: " + ctx.getSuite().getXmlSuite().getFileName() + ")");
    }

    @Override
    public void onFinish(ITestContext context) {
        logResults();
        suiteName = null;
    }

    private ITestNGMethod[] resultsToMethods(List<ITestResult> results) {
        ITestNGMethod[] result = new ITestNGMethod[results.size()];
        int i = 0;
        for (ITestResult tr : results) {
            result[i++] = tr.getMethod();
        }
        return result;
    }

    /**
     * Print out test summary
     */
    private void logResults() {
        //
        // Log test summary
        //
        ITestNGMethod[] ft = resultsToMethods(getFailedTests());
        StringBuilder sb = new StringBuilder("\n===============================================\n");
        sb.append("    ").append(suiteName).append("\n");
        sb.append("    Tests run: ").append(Utils.calculateInvokedMethodCount(getAllTestMethods()));
        sb.append(", Failures: ").append(Utils.calculateInvokedMethodCount(ft));
        sb.append(", Skips: ").append(Utils.calculateInvokedMethodCount(resultsToMethods(getSkippedTests())));
        int confFailures = getConfigurationFailures().size();
        int confSkips = getConfigurationSkips().size();
        if (confFailures > 0 || confSkips > 0) {
            sb.append("\n").append("    Configuration Failures: ").append(confFailures);
            sb.append(", Skips: ").append(confSkips);
        }
        sb.append("\n===============================================");
        log(sb.toString());
    }

    /**
     * Log meaningful message for passed in arguments.
     * Message itself is of form:
     * $status: "$suiteName" - $methodDeclaration ($actualArguments) finished in $x ms ($run of $totalRuns)
     *
     * @param st status of passed in itr
     * @param itr test result to be described
     * @param isConfMethod is itr describing configuration method
     */
    private void logTestResult(Status st, ITestResult itr, boolean isConfMethod) {
        StringBuilder sb = new StringBuilder();
        StringBuilder succRate = null;
        String stackTrace = "";
        switch (st) {
            case STARTED:
                sb.append("INVOKING");
                break;
            case SKIP:
                sb.append("SKIPPED");
                stackTrace = itr.getThrowable() != null
                        ? Utils.stackTrace(itr.getThrowable(), false)[0] : "";
                break;
            case FAILURE:
                sb.append("FAILED");
                stackTrace = itr.getThrowable() != null
                        ? Utils.stackTrace(itr.getThrowable(), false)[0] : "";
                break;
            case SUCCESS:
                sb.append("PASSED");
                break;
            case SUCCESS_PERCENTAGE_FAILURE:
                sb.append("PASSED with failures");
                break;
            default:
                //not happen
                throw new RuntimeException("Unsupported test status:" + itr.getStatus());
        }
        if (isConfMethod) {
            sb.append(" CONFIGURATION: ");
        } else {
            sb.append(": ");
        }
        ITestNGMethod tm = itr.getMethod();
        int identLevel = sb.length();
        sb.append(getMethodDeclaration(tm));
        Object[] params = itr.getParameters();
        Class[] paramTypes = tm.getMethod().getParameterTypes();
        if (null != params && params.length > 0) {
            // The error might be a data provider parameter mismatch, so make
            // a special case here
            if (params.length != paramTypes.length) {
                sb.append("Wrong number of arguments were passed by the Data Provider: found ");
                sb.append(params.length);
                sb.append(" but expected ");
                sb.append(paramTypes.length);
            } else {
                sb.append("(value(s): ");
                for (int i = 0; i < params.length; i++) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    sb.append(Utils.toString(params[i], paramTypes[i]));
                }
                sb.append(")");

            }
        }
        if (Status.STARTED != st) {
            sb.append(" finished in ");
            sb.append(itr.getEndMillis() - itr.getStartMillis());
            sb.append(" ms");
            if (!Utils.isStringEmpty(tm.getDescription())) {
                sb.append("\n");
                for (int i = 0; i < identLevel; i++) {
                    sb.append(" ");
                }
                sb.append(tm.getDescription());
            }
            if (tm.getInvocationCount() > 1) {
                sb.append(" (");
                sb.append(tm.getCurrentInvocationCount());
                sb.append(" of ");
                sb.append(tm.getInvocationCount());
                sb.append(")");
            }
            if (!Utils.isStringEmpty(stackTrace)) {
                sb.append("\n").append(stackTrace.substring(0, stackTrace.lastIndexOf(System.getProperty("line.separator"))));
            }
        } else {
            if (!isConfMethod && tm.getInvocationCount() > 1) {
                sb.append(" success: ");
                sb.append(tm.getSuccessPercentage());
                sb.append("%");
            }
        }
        log(sb.toString());
    }

    protected void log(String message) {
        //prefix all output lines
        System.out.println(message.replaceAll("(?m)^", prefix));
    }

    /**
     *
     * @param method method to be described
     * @return FQN of a class + method declaration for a method passed in
     *      ie. test.triangle.CheckCount.testCheckCount(java.lang.String)
     */
    private String getMethodDeclaration(ITestNGMethod method) {
        //see Utils.detailedMethodName
        //perhaps should rather adopt the original method instead
        Method m = method.getMethod();
        StringBuilder buf = new StringBuilder();
        buf.append("\"");
        if (suiteName != null) {
            buf.append(suiteName);
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
        return buf.toString();
    }

    @Override
    public String toString() {
        return "VerboseReporter{" + "suiteName=" + suiteName + '}';
    }
}
