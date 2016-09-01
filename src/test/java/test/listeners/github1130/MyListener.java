package test.listeners.github1130;

import org.testng.IClassListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestResult;

public class MyListener implements ISuiteListener, IClassListener {

    public static int beforeSuiteCount = 0;
    public static int beforeClassCount = 0;

    public MyListener() {
        System.out.println("Instantiate new MyListener");
    }

    public void onStart(ISuite suite) {
        beforeSuiteCount++;
    }

    public void onBeforeClass(ITestClass testClass) {
        beforeClassCount++;
    }

    public void onTestStart(ITestResult result) {
    }

    public void onTestSuccess(ITestResult result) {
    }

    public void onTestFailure(ITestResult result) {
    }

    public void onTestSkipped(ITestResult result) {
    }

    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    }

    public void onStart(ITestContext context) {
    }

    public void onFinish(ITestContext context) {
    }

    public void onFinish(ISuite suite) {
    }

    public void onAfterClass(ITestClass testClass) {
    }
}
