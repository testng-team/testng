package test.inject;

import org.testng.ITestResult;
import org.testng.annotations.Test;

public class NegativeNativeInjectionTestSample {

    @Test
    public void m1(ITestResult result) {
    }

    @Test
    public void m2(int foo) {
    }
}
