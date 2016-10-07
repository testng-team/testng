package test.thread.parallelization.sample;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import test.thread.parallelization.TestNgRunStateTracker;

import java.util.concurrent.TimeUnit;

public class TestClassDThreeMethodsWithNoDepsSample {

    @Parameters({ "suiteName", "testName", "sleepFor" })
    @Test
    public void testMethodA(String suiteName, String testName, String sleepFor) throws InterruptedException {
        long time = System.currentTimeMillis();

        TestNgRunStateTracker.logEvent(
                TestNgRunStateTracker.EventLog.builder()
                        .setEvent(TestNgRunStateTracker.TestNgRunEvent.TEST_METHOD_EXECUTION)
                        .setTimeOfEvent(time)
                        .setThread(Thread.currentThread())
                        .addData(TestNgRunStateTracker.EventInfo.METHOD_NAME, "testMethodA")
                        .addData(TestNgRunStateTracker.EventInfo.CLASS_NAME, getClass().getCanonicalName())
                        .addData(TestNgRunStateTracker.EventInfo.CLASS_INSTANCE, this)
                        .addData(TestNgRunStateTracker.EventInfo.TEST_NAME, testName)
                        .addData(TestNgRunStateTracker.EventInfo.SUITE_NAME, suiteName)
                        .build()
        );

        TimeUnit.SECONDS.sleep(Integer.parseInt(sleepFor));
    }

    @Parameters({ "suiteName", "testName", "sleepFor" })
    @Test
    public void testMethodB(String suiteName, String testName, String sleepFor) throws InterruptedException {
        long time = System.currentTimeMillis();

        TestNgRunStateTracker.logEvent(
                TestNgRunStateTracker.EventLog.builder()
                        .setEvent(TestNgRunStateTracker.TestNgRunEvent.TEST_METHOD_EXECUTION)
                        .setTimeOfEvent(time)
                        .setThread(Thread.currentThread())
                        .addData(TestNgRunStateTracker.EventInfo.METHOD_NAME, "testMethodB")
                        .addData(TestNgRunStateTracker.EventInfo.CLASS_NAME, getClass().getCanonicalName())
                        .addData(TestNgRunStateTracker.EventInfo.CLASS_INSTANCE, this)
                        .addData(TestNgRunStateTracker.EventInfo.TEST_NAME, testName)
                        .addData(TestNgRunStateTracker.EventInfo.SUITE_NAME, suiteName)
                        .build()
        );

        TimeUnit.SECONDS.sleep(Integer.parseInt(sleepFor));
    }

    @Parameters({ "suiteName", "testName", "sleepFor" })
    @Test
    public void testMethodC(String suiteName, String testName, String sleepFor) throws InterruptedException {
        long time = System.currentTimeMillis();

        TestNgRunStateTracker.logEvent(
                TestNgRunStateTracker.EventLog.builder()
                        .setEvent(TestNgRunStateTracker.TestNgRunEvent.TEST_METHOD_EXECUTION)
                        .setTimeOfEvent(time)
                        .setThread(Thread.currentThread())
                        .addData(TestNgRunStateTracker.EventInfo.METHOD_NAME, "testMethodC")
                        .addData(TestNgRunStateTracker.EventInfo.CLASS_NAME, getClass().getCanonicalName())
                        .addData(TestNgRunStateTracker.EventInfo.CLASS_INSTANCE, this)
                        .addData(TestNgRunStateTracker.EventInfo.TEST_NAME, testName)
                        .addData(TestNgRunStateTracker.EventInfo.SUITE_NAME, suiteName)
                        .build()
        );

        TimeUnit.SECONDS.sleep(Integer.parseInt(sleepFor));
    }
}
