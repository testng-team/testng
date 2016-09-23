package test.thread.parallelization;

import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.concurrent.TimeUnit;

public class TestNgRunStateListener implements ISuiteListener, ITestListener {
    @Override
    public void onStart(ISuite suite) {
        long time = System.currentTimeMillis();

        TestNgRunStateTracker.logEvent(
                TestNgRunStateTracker.EventLog.builder()
                        .setEvent(TestNgRunStateTracker.TestNgRunEvent.LISTENER_SUITE_START)
                        .setTimeOfEvent(time)
                        .setThreadId(Thread.currentThread().getId())
                        .addData(TestNgRunStateTracker.EventInfo.SUITE_NAME, suite.getName())
                        .build()
        );

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch(InterruptedException e) {
            throw new RuntimeException("Problem with delaying after suite listener onStart", e);
        }
    }

    @Override
    public void onFinish(ISuite suite) {
        long time = System.currentTimeMillis();

        TestNgRunStateTracker.logEvent(
                TestNgRunStateTracker.EventLog.builder()
                        .setEvent(TestNgRunStateTracker.TestNgRunEvent.LISTENER_SUITE_FINISH)
                        .setTimeOfEvent(time)
                        .setThreadId(Thread.currentThread().getId())
                        .addData(TestNgRunStateTracker.EventInfo.SUITE_NAME, suite.getName())
                        .build()
        );

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch(InterruptedException e) {
            throw new RuntimeException("Problem with delaying after suite listener onFinish", e);
        }
    }

    @Override
    public void onStart(ITestContext context) {
        long time = System.currentTimeMillis();

        TestNgRunStateTracker.logEvent(
                TestNgRunStateTracker.EventLog.builder()
                        .setEvent(TestNgRunStateTracker.TestNgRunEvent.LISTENER_TEST_START)
                        .setTimeOfEvent(time)
                        .setThreadId(Thread.currentThread().getId())
                        .addData(TestNgRunStateTracker.EventInfo.TEST_NAME, context.getName())
                        .addData(TestNgRunStateTracker.EventInfo.SUITE_NAME, context.getSuite().getName())
                        .build()
        );

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch(InterruptedException e) {
            throw new RuntimeException("Problem with delaying after test listener onStart", e);
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        long time = System.currentTimeMillis();

        TestNgRunStateTracker.logEvent(
                TestNgRunStateTracker.EventLog.builder()
                        .setEvent(TestNgRunStateTracker.TestNgRunEvent.LISTENER_TEST_FINISH)
                        .setTimeOfEvent(time)
                        .setThreadId(Thread.currentThread().getId())
                        .addData(TestNgRunStateTracker.EventInfo.TEST_NAME, context.getName())
                        .addData(TestNgRunStateTracker.EventInfo.SUITE_NAME, context.getSuite().getName())
                        .build()
        );

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch(InterruptedException e) {
            throw new RuntimeException("Problem with delaying after test listener onFinish", e);
        }
    }


    @Override
    public void onTestStart(ITestResult result) {
        long time = System.currentTimeMillis();

        TestNgRunStateTracker.logEvent(
                TestNgRunStateTracker.EventLog.builder()
                        .setEvent(TestNgRunStateTracker.TestNgRunEvent.LISTENER_TEST_METHOD_START)
                        .setTimeOfEvent(time)
                        .setThreadId(Thread.currentThread().getId())
                        .addData(TestNgRunStateTracker.EventInfo.METHOD_NAME, result.getMethod().getMethodName())
                        .addData(TestNgRunStateTracker.EventInfo.CLASS_NAME, result.getMethod().getRealClass()
                                .getCanonicalName())
                        .addData(TestNgRunStateTracker.EventInfo.CLASS_INSTANCE, result.getMethod().getInstance())
                        .addData(TestNgRunStateTracker.EventInfo.TEST_NAME, result.getTestContext().getName())
                        .addData(TestNgRunStateTracker.EventInfo.SUITE_NAME, result.getTestContext().getSuite()
                                .getName())
                        .build()
        );

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch(InterruptedException e) {
            throw new RuntimeException("Problem with delaying after test method listener onTestStart", e);
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        long time = System.currentTimeMillis();

        TestNgRunStateTracker.logEvent(
                TestNgRunStateTracker.EventLog.builder()
                        .setEvent(TestNgRunStateTracker.TestNgRunEvent.LISTENER_TEST_METHOD_PASS)
                        .setTimeOfEvent(time)
                        .setThreadId(Thread.currentThread().getId())
                        .addData(TestNgRunStateTracker.EventInfo.METHOD_NAME, result.getMethod().getMethodName())
                        .addData(TestNgRunStateTracker.EventInfo.CLASS_NAME, result.getMethod().getRealClass()
                                .getCanonicalName())
                        .addData(TestNgRunStateTracker.EventInfo.CLASS_INSTANCE, result.getMethod().getInstance())
                        .addData(TestNgRunStateTracker.EventInfo.TEST_NAME, result.getTestContext().getName())
                        .addData(TestNgRunStateTracker.EventInfo.SUITE_NAME, result.getTestContext().getSuite()
                                .getName())
                        .build()
        );

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch(InterruptedException e) {
            throw new RuntimeException("Problem with delaying after test method listener onTestSuccess", e);
        }
    }


    @Override
    public void onTestFailure(ITestResult result) {
        long time = System.currentTimeMillis();

        TestNgRunStateTracker.logEvent(
                TestNgRunStateTracker.EventLog.builder()
                        .setEvent(TestNgRunStateTracker.TestNgRunEvent.LISTENER_TEST_METHOD_FAIL)
                        .setTimeOfEvent(time)
                        .setThreadId(Thread.currentThread().getId())
                        .addData(TestNgRunStateTracker.EventInfo.METHOD_NAME, result.getMethod().getMethodName())
                        .addData(TestNgRunStateTracker.EventInfo.CLASS_NAME, result.getMethod().getRealClass()
                                .getCanonicalName())
                        .addData(TestNgRunStateTracker.EventInfo.CLASS_INSTANCE, result.getMethod().getInstance())
                        .addData(TestNgRunStateTracker.EventInfo.TEST_NAME, result.getTestContext().getName())
                        .addData(TestNgRunStateTracker.EventInfo.SUITE_NAME, result.getTestContext().getSuite()
                                .getName())
                        .build()
        );

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch(InterruptedException e) {
            throw new RuntimeException("Problem with delaying after test method listener onTestFailure", e);
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        long time = System.currentTimeMillis();

        TestNgRunStateTracker.logEvent(
                TestNgRunStateTracker.EventLog.builder()
                        .setEvent(TestNgRunStateTracker.TestNgRunEvent.LISTENER_TEST_METHOD_SKIPPED)
                        .setTimeOfEvent(time)
                        .setThreadId(Thread.currentThread().getId())
                        .addData(TestNgRunStateTracker.EventInfo.METHOD_NAME, result.getMethod().getMethodName())
                        .addData(TestNgRunStateTracker.EventInfo.CLASS_NAME, result.getMethod().getRealClass()
                                .getCanonicalName())
                        .addData(TestNgRunStateTracker.EventInfo.CLASS_INSTANCE, result.getMethod().getInstance())
                        .addData(TestNgRunStateTracker.EventInfo.TEST_NAME, result.getTestContext().getName())
                        .addData(TestNgRunStateTracker.EventInfo.SUITE_NAME, result.getTestContext().getSuite()
                                .getName())
                        .build()
        );

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch(InterruptedException e) {
            throw new RuntimeException("Problem with delaying after test method listener onTestSkipped", e);
        }

    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        long time = System.currentTimeMillis();

        TestNgRunStateTracker.logEvent(
                TestNgRunStateTracker.EventLog.builder()
                        .setEvent(TestNgRunStateTracker.TestNgRunEvent.LISTENER_TEST_METHOD_FAIL_PERCENTAGE)
                        .setTimeOfEvent(time)
                        .setThreadId(Thread.currentThread().getId())
                        .addData(TestNgRunStateTracker.EventInfo.METHOD_NAME, result.getMethod().getMethodName())
                        .addData(TestNgRunStateTracker.EventInfo.CLASS_NAME, result.getMethod().getRealClass()
                                .getCanonicalName())
                        .addData(TestNgRunStateTracker.EventInfo.CLASS_INSTANCE, result.getMethod().getInstance())
                        .addData(TestNgRunStateTracker.EventInfo.TEST_NAME, result.getTestContext().getName())
                        .addData(TestNgRunStateTracker.EventInfo.SUITE_NAME, result.getTestContext().getSuite()
                                .getName())
                        .build()
        );

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch(InterruptedException e) {
            throw new RuntimeException("Problem with delaying after test method listener " +
                    "onTestFailedButWithinSuccessPercentage", e);
        }
    }
}
