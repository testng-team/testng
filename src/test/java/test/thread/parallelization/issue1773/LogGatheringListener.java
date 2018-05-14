package test.thread.parallelization.issue1773;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.collections.Sets;

import java.util.Set;

public class LogGatheringListener implements IInvokedMethodListener {

    Set<String> log = Sets.newHashSet();

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        log.addAll(Reporter.getOutput(testResult));
    }
}
