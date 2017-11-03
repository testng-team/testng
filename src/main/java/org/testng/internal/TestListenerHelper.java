package org.testng.internal;

import org.testng.IConfigurationListener;
import org.testng.IConfigurationListener2;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.List;

/**
 * A helper class that internally houses some of the listener related actions support.
 */
public final class TestListenerHelper {
    private TestListenerHelper() {
        //Utility class. Defeat instantiation.
    }

    static void runPreConfigurationListeners(ITestResult tr, List<IConfigurationListener> listeners) {
        for (IConfigurationListener icl : listeners) {
            if (icl instanceof IConfigurationListener2) {
                ((IConfigurationListener2) icl).beforeConfiguration(tr);
            }
        }
    }

    static void runPostConfigurationListeners(ITestResult tr, List<IConfigurationListener> listeners) {
        for (IConfigurationListener icl : listeners) {
            switch (tr.getStatus()) {
                case ITestResult.SKIP:
                    icl.onConfigurationSkip(tr);
                    break;
                case ITestResult.FAILURE:
                    icl.onConfigurationFailure(tr);
                    break;
                case ITestResult.SUCCESS:
                    icl.onConfigurationSuccess(tr);
                    break;
                default:
                    throw new AssertionError("Unexpected value: " + tr.getStatus());
            }
        }
    }

    /**
     * Iterates through a bunch of listeners and invokes them.
     *
     * @param tr        - The {@link ITestResult} object that is to be fed into a listener when invoking it.
     * @param listeners - A list of {@link ITestListener} objects which are to be invoked.
     */
    public static void runTestListeners(ITestResult tr, List<ITestListener> listeners) {
        for (ITestListener itl : listeners) {
            switch (tr.getStatus()) {
                case ITestResult.SKIP:
                    itl.onTestSkipped(tr);
                    break;
                case ITestResult.SUCCESS_PERCENTAGE_FAILURE:
                    itl.onTestFailedButWithinSuccessPercentage(tr);
                    break;
                case ITestResult.FAILURE:
                    itl.onTestFailure(tr);
                    break;
                case ITestResult.SUCCESS:
                    itl.onTestSuccess(tr);
                    break;
                case ITestResult.STARTED:
                    itl.onTestStart(tr);
                    break;
                default:
                    throw new AssertionError("Unknown status: " + tr.getStatus());
            }
        }
    }
}
