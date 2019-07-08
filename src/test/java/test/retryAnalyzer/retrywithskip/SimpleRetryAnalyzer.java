package test.retryAnalyzer.retrywithskip;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class SimpleRetryAnalyzer implements IRetryAnalyzer {
    private static int counts = 1;
    @Override
    public boolean retry(ITestResult result) {
        if(counts -- > 0) {
            return true;
        }else {
            counts = 1 ;
            return false;
        }
    }
}
