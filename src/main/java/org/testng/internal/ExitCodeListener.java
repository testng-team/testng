package org.testng.internal;

import org.testng.*;
import org.testng.reporters.XMLReporterConfig;
import org.testng.xml.XmlSuite;

import java.util.List;

public class ExitCodeListener implements ITestListener, IReporter, IReporter2 {
    private boolean hasTests = false;
    private final ExitCode status = new ExitCode();

    public ExitCode getStatus() {
        return status;
    }

    public boolean hasTests() {
        return hasTests;
    }

    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
        for (ISuite suite : suites) {
            for (ISuiteResult suiteResult : suite.getResults().values()) {
                ITestContext context = suiteResult.getTestContext();
                status.computeAndUpdate(context);
            }
        }
    }

    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, XMLReporterConfig config) {
        for (ISuite suite : suites) {
            for (ISuiteResult suiteResult : suite.getResults().values()) {
                ITestContext context = suiteResult.getTestContext();
                status.computeAndUpdate(context);
            }
        }
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
