package test.thread.parallelization.sample;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import test.thread.parallelization.TestNgRunStateTracker;

import java.util.concurrent.TimeUnit;

import static test.thread.parallelization.TestNgRunStateTracker.EventInfo.CLASS_INSTANCE;
import static test.thread.parallelization.TestNgRunStateTracker.EventInfo.CLASS_NAME;
import static test.thread.parallelization.TestNgRunStateTracker.EventInfo.GROUPS_BELONGING_TO;
import static test.thread.parallelization.TestNgRunStateTracker.EventInfo.GROUPS_DEPENDED_ON;
import static test.thread.parallelization.TestNgRunStateTracker.EventInfo.METHODS_DEPENDED_ON;
import static test.thread.parallelization.TestNgRunStateTracker.EventInfo.METHOD_NAME;
import static test.thread.parallelization.TestNgRunStateTracker.EventInfo.SUITE_NAME;
import static test.thread.parallelization.TestNgRunStateTracker.EventInfo.TEST_NAME;
import static test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent.TEST_METHOD_EXECUTION;

public class TestClassBSixMethodsWithMethodOnMethodDependenciesSample {

    @Parameters({ "suiteName", "testName", "sleepFor" })
    @Test
    public void testMethodA(String suiteName, String testName, String sleepFor) throws InterruptedException {
        long time = System.currentTimeMillis();

        TestNgRunStateTracker.logEvent(
                TestNgRunStateTracker.EventLog.builder()
                        .setEvent(TEST_METHOD_EXECUTION)
                        .setTimeOfEvent(time)
                        .setThread(Thread.currentThread())
                        .addData(METHOD_NAME, "testMethodA")
                        .addData(CLASS_NAME, getClass().getCanonicalName())
                        .addData(CLASS_INSTANCE, this)
                        .addData(TEST_NAME, testName)
                        .addData(SUITE_NAME, suiteName)
                        .addData(GROUPS_DEPENDED_ON, new String[0])
                        .addData(METHODS_DEPENDED_ON, new String[0])
                        .addData(GROUPS_BELONGING_TO, new String[0])
                        .build()
        );

        TimeUnit.MILLISECONDS.sleep(Integer.parseInt(sleepFor));
    }

    @Parameters({ "suiteName", "testName", "sleepFor" })
    @Test(dependsOnMethods = "testMethodA")
    public void testMethodB(String suiteName, String testName, String sleepFor) throws InterruptedException {
        long time = System.currentTimeMillis();

        TestNgRunStateTracker.logEvent(
                TestNgRunStateTracker.EventLog.builder()
                        .setEvent(TEST_METHOD_EXECUTION)
                        .setTimeOfEvent(time)
                        .setThread(Thread.currentThread())
                        .addData(METHOD_NAME, "testMethodB")
                        .addData(CLASS_NAME, getClass().getCanonicalName())
                        .addData(CLASS_INSTANCE, this)
                        .addData(TEST_NAME, testName)
                        .addData(SUITE_NAME, suiteName)
                        .addData(GROUPS_DEPENDED_ON, new String[0])
                        .addData(METHODS_DEPENDED_ON, new String[]{this.getClass().getCanonicalName() + ".testMethodA"})
                        .addData(GROUPS_BELONGING_TO, new String[0])
                        .build()
        );

        TimeUnit.MILLISECONDS.sleep(Integer.parseInt(sleepFor));
    }

    @Parameters({ "suiteName", "testName", "sleepFor" })
    @Test(dependsOnMethods = "testMethodA")
    public void testMethodC(String suiteName, String testName, String sleepFor) throws InterruptedException {
        long time = System.currentTimeMillis();

        TestNgRunStateTracker.logEvent(
                TestNgRunStateTracker.EventLog.builder()
                        .setEvent(TEST_METHOD_EXECUTION)
                        .setTimeOfEvent(time)
                        .setThread(Thread.currentThread())
                        .addData(METHOD_NAME, "testMethodC")
                        .addData(CLASS_NAME, getClass().getCanonicalName())
                        .addData(CLASS_INSTANCE, this)
                        .addData(TEST_NAME, testName)
                        .addData(SUITE_NAME, suiteName)
                        .addData(GROUPS_DEPENDED_ON, new String[0])
                        .addData(METHODS_DEPENDED_ON, new String[]{this.getClass().getCanonicalName() + ".testMethodA"})
                        .addData(GROUPS_BELONGING_TO, new String[0])
                        .build()
        );

        TimeUnit.MILLISECONDS.sleep(Integer.parseInt(sleepFor));
    }

    @Parameters({ "suiteName", "testName", "sleepFor" })
    @Test(dependsOnMethods = "testMethodC")
    public void testMethodD(String suiteName, String testName, String sleepFor) throws InterruptedException {
        long time = System.currentTimeMillis();

        TestNgRunStateTracker.logEvent(
                TestNgRunStateTracker.EventLog.builder()
                        .setEvent(TEST_METHOD_EXECUTION)
                        .setTimeOfEvent(time)
                        .setThread(Thread.currentThread())
                        .addData(METHOD_NAME, "testMethodD")
                        .addData(CLASS_NAME, getClass().getCanonicalName())
                        .addData(CLASS_INSTANCE, this)
                        .addData(TEST_NAME, testName)
                        .addData(SUITE_NAME, suiteName)
                        .addData(GROUPS_DEPENDED_ON, new String[0])
                        .addData(METHODS_DEPENDED_ON, new String[]{this.getClass().getCanonicalName() + ".testMethodC"})
                        .addData(GROUPS_BELONGING_TO, new String[0])
                        .build()
        );

        TimeUnit.MILLISECONDS.sleep(Integer.parseInt(sleepFor));
    }

    @Parameters({ "suiteName", "testName", "sleepFor" })
    @Test(dependsOnMethods = "testMethodD")
    public void testMethodE(String suiteName, String testName, String sleepFor) throws InterruptedException {
        long time = System.currentTimeMillis();

        TestNgRunStateTracker.logEvent(
                TestNgRunStateTracker.EventLog.builder()
                        .setEvent(TEST_METHOD_EXECUTION)
                        .setTimeOfEvent(time)
                        .setThread(Thread.currentThread())
                        .addData(METHOD_NAME, "testMethodE")
                        .addData(CLASS_NAME, getClass().getCanonicalName())
                        .addData(CLASS_INSTANCE, this)
                        .addData(TEST_NAME, testName)
                        .addData(SUITE_NAME, suiteName)
                        .addData(GROUPS_DEPENDED_ON, new String[0])
                        .addData(METHODS_DEPENDED_ON, new String[]{this.getClass().getCanonicalName() + ".testMethodD"})
                        .addData(GROUPS_BELONGING_TO, new String[0])
                        .build()
        );

        TimeUnit.MILLISECONDS.sleep(Integer.parseInt(sleepFor));
    }

    @Parameters({ "suiteName", "testName", "sleepFor" })
    @Test(dependsOnMethods = "testMethodE")
    public void testMethodF(String suiteName, String testName, String sleepFor) throws InterruptedException {
        long time = System.currentTimeMillis();

        TestNgRunStateTracker.logEvent(
                TestNgRunStateTracker.EventLog.builder()
                        .setEvent(TEST_METHOD_EXECUTION)
                        .setTimeOfEvent(time)
                        .setThread(Thread.currentThread())
                        .addData(METHOD_NAME, "testMethodF")
                        .addData(CLASS_NAME, getClass().getCanonicalName())
                        .addData(CLASS_INSTANCE, this)
                        .addData(TEST_NAME, testName)
                        .addData(SUITE_NAME, suiteName)
                        .addData(GROUPS_DEPENDED_ON, new String[0])
                        .addData(METHODS_DEPENDED_ON, new String[]{this.getClass().getCanonicalName() + ".testMethodE"})
                        .addData(GROUPS_BELONGING_TO, new String[0])
                        .build()
        );

        TimeUnit.MILLISECONDS.sleep(Integer.parseInt(sleepFor));
    }
}
