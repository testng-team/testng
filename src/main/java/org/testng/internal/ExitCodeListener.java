package org.testng.internal;

import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;

import java.util.BitSet;
import java.util.List;

public class ExitCodeListener implements ITestListener, IReporter {
    private boolean hasTests = false;
    private int status;

    public int getStatus() {
        return status;
    }

    public boolean isHasTests() {
        return hasTests;
    }

    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
        BitSet initial = new BitSet(3);
        initial.set(0, false); // Success bit
        initial.set(1, false); // failed bit
        initial.set(2, false); // skipped bit
        for (ISuite suite : suites) {
            for (ISuiteResult suiteResult : suite.getResults().values()) {
                ITestContext context = suiteResult.getTestContext();
                boolean passed = (context.getPassedTests().size() != 0) ||
                        (context.getPassedConfigurations().size() != 0);
                initial.set(0, passed);
                boolean failed = (context.getFailedTests().size() != 0) ||
                        (context.getFailedConfigurations().size() != 0) ||
                        (context.getFailedButWithinSuccessPercentageTests().size() !=0);
                initial.set(1, failed);
                boolean skipped = (context.getSkippedTests().size() != 0) ||
                        (context.getSkippedConfigurations().size() != 0);
                initial.set(2, skipped);

            }
        }
        status = convert(initial);
    }

    private static int convert(BitSet set) {
        int size = set.length() - 1;
        int value = 0;
        for (int i= size;i >=0;i--) {
            int prefix = 0;
            if (set.get(i)) {
                prefix = 1;
            }
            value += Math.pow(2, i) * prefix;
        }
        return transform(value);
    }


    private static int transform(int value) {
        if (value ==0 || value == 1) {
            return 0;
        }
        return value;
    }

    @Override
    public void onTestStart(ITestResult result) {
        this.hasTests = true;
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        this.hasTests = true;
    }

    @Override
    public void onTestFailure(ITestResult result) {
        this.hasTests = true;
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        this.hasTests = true;
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        this.hasTests = true;
    }

    @Override
    public void onStart(ITestContext context) {

    }

    @Override
    public void onFinish(ITestContext context) {

    }
}
