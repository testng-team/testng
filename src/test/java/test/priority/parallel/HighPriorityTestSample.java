package test.priority.parallel;

import java.util.concurrent.TimeUnit;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import static test.thread.parallelization.TestNgRunStateTracker.EventInfo.*;
import static test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent.TEST_METHOD_EXECUTION;

import test.thread.parallelization.TestNgRunStateTracker;

public class HighPriorityTestSample {
    
    @Parameters({ "suiteName", "testName", "sleepFor" })
    @Test(priority = -10)
    public void slowMethod(String suiteName, String testName, String sleepFor) throws InterruptedException {
        long time = System.currentTimeMillis();

        TestNgRunStateTracker.logEvent(
                TestNgRunStateTracker.EventLog.builder()
                        .setEvent(TEST_METHOD_EXECUTION)
                        .setTimeOfEvent(time)
                        .setThread(Thread.currentThread())
                        .addData(METHOD_NAME, "slowMethod")
                        .addData(CLASS_NAME, getClass().getCanonicalName())
                        .addData(CLASS_INSTANCE, this)
                        .addData(TEST_NAME, testName)
                        .addData(SUITE_NAME, suiteName)
                        .addData(GROUPS_DEPENDED_ON, new String[0])
                        .addData(METHODS_DEPENDED_ON, new String[0])
                        .addData(GROUPS_BELONGING_TO, new String[0])
                        .build()
        );

        // This is supposed to be the slow one, so multiply by 80 so we sleep extra long.
        TimeUnit.MILLISECONDS.sleep(80 * Integer.parseInt(sleepFor));
    }
}
