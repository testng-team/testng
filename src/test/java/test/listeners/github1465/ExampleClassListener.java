package test.listeners.github1465;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public class ExampleClassListener implements IInvokedMethodListener {
    final List<String> messages = new LinkedList<>();
    final List<String> configMsgs = new LinkedList<>();

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        log("beforeInvocation:", method, testResult);
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        log("afterInvocation:", method, testResult);
    }

    private void log(String prefix, IInvokedMethod method, ITestResult testResult) {
        String msg = prefix + "_" + typeOfMethod(method);
        msg += method.getTestMethod().getMethodName() + parameters(testResult);
        if (method.isConfigurationMethod()) {
            configMsgs.add(msg);
        } else {
            messages.add(msg);
        }
    }

    private static String typeOfMethod(IInvokedMethod method) {
        ITestNGMethod tm = method.getTestMethod();
        if (tm.isBeforeMethodConfiguration()) {
            return "before_method: ";
        }
        if (tm.isAfterMethodConfiguration()) {
            return "after_method: ";
        }
        return "test_method: ";
    }

    private static String parameters(ITestResult testResult) {
        Object[] parameters = testResult.getParameters();
        if (parameters == null) {
            return "";
        }
        String returnValue = "";
        StringBuilder builder = new StringBuilder();
        for (Object parameter : parameters) {
            if (parameter instanceof Method) {
                builder.append(((Method) parameter).getName());
            }
        }
        if (builder.length() != 0) {
            returnValue =  "[" + builder.toString() + "]";
        }
        return returnValue;

    }
}
