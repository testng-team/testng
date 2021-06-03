package test.retryAnalyzer;

import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import java.util.Set;

public class TestResultPruner extends TestListenerAdapter {

    @Override
    public void onFinish(ITestContext context) {
        for (ITestNGMethod method : context.getAllTestMethods()) {
            Set<ITestResult> passed = context.getPassedTests().getResults(method);
            Set<ITestResult> skipped = context.getSkippedTests().getResults(method);
            Set<ITestResult> failed = context.getFailedTests().getResults(method);
            Set<ITestResult> failedWithinSuccess = context.getFailedButWithinSuccessPercentageTests().getResults(method);
            if (!passed.isEmpty() && !skipped.isEmpty()) {
                context.getSkippedTests().removeResult(method);
            }
            if (((!failedWithinSuccess.isEmpty()) || (!failed.isEmpty())) && !skipped.isEmpty()) {
                context.getSkippedTests().removeResult(method);
            }
        }
    }
}
