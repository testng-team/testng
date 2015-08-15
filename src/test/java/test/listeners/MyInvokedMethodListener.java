package test.listeners;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

import java.util.HashMap;
import java.util.Map;

public class MyInvokedMethodListener implements IInvokedMethodListener {

    public static Map<String, Integer> beforeInvocation = new HashMap<>();
    public static Map<String, Integer> afterInvocation = new HashMap<>();


    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        increments(beforeInvocation, method);
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        increments(afterInvocation, method);
    }

    private static void increments(Map<String, Integer> map, IInvokedMethod method) {
        String stringValue = method.getTestMethod().getMethodName();
        Integer count = map.get(stringValue);
        if (count == null) {
            count = 0;
        }
        map.put(stringValue, count+1);
    }
}
