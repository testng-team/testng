package test.listeners.github2415;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.SkipException;

import java.util.HashMap;
import java.util.Map;

public class SkipAfterPreviousTestClassFailureListener implements IInvokedMethodListener {

    private boolean hasFailures = false;
    public static Map<String, Integer> status = new HashMap<>();

    public void beforeInvocation(IInvokedMethod invokedMethod, ITestResult testResult) {
        status.put(testResult.getTestClass().getName() + testResult.getName(), testResult.getStatus());
        if (hasFailures) {
            throw new SkipException("SkipException");
        }
    }

    public void afterInvocation(IInvokedMethod invokedMethod, ITestResult testResult) {
        status.put(testResult.getTestClass().getName() + testResult.getName(), testResult.getStatus());
        if (! invokedMethod.isTestMethod()) {
            return;
        }
        if (! testResult.isSuccess()) {
            hasFailures = true;
        }
    }
}