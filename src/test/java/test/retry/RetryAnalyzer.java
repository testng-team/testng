package test.dataprovider.retry;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {
    int retryCount = 1;

    @Override
    public boolean retry(ITestResult result) {
        return retryCount-- < 0;
    }
}
