package test.factory.github2560;

import org.testng.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InvokedMethodListener implements IInvokedMethodListener {

    final Map<Integer, List<String>> capturedBeforeInvocations = new ConcurrentHashMap<>();
    final Map<Integer, List<String>> capturedAfterInvocations = new ConcurrentHashMap<>();

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult, ITestContext context) {
        Assert.assertSame(method.getTestMethod().getInstance(), testResult.getInstance());
        capturedBeforeInvocations.computeIfAbsent(testResult.getInstance().hashCode(), ignored -> new ArrayList<>())
                .add(method.getTestMethod().getMethodName());
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult, ITestContext context) {
        Assert.assertSame(method.getTestMethod().getInstance(), testResult.getInstance());
        capturedAfterInvocations.computeIfAbsent(testResult.getInstance().hashCode(), ignored -> new ArrayList<>())
                .add(method.getTestMethod().getMethodName());
    }
}
