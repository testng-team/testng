package test.priority.parallel;

import static test.thread.parallelization.TestNgRunStateTracker.EventInfo.*;
import static test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent.TEST_METHOD_EXECUTION;

import java.util.concurrent.TimeUnit;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import test.thread.parallelization.TestNgRunStateTracker;

public class LowPriorityTestSample {

  @Parameters({"suiteName", "testName", "sleepFor"})
  @Test(priority = 10)
  public void testMethodA(String suiteName, String testName, String sleepFor)
      throws InterruptedException {
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
            .build());

    TimeUnit.MILLISECONDS.sleep(Integer.parseInt(sleepFor));
  }

  @Parameters({"suiteName", "testName", "sleepFor"})
  @Test(priority = 10)
  public void testMethodB(String suiteName, String testName, String sleepFor)
      throws InterruptedException {
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
            .addData(METHODS_DEPENDED_ON, new String[0])
            .addData(GROUPS_BELONGING_TO, new String[0])
            .build());

    TimeUnit.MILLISECONDS.sleep(Integer.parseInt(sleepFor));
  }

  @Parameters({"suiteName", "testName", "sleepFor"})
  @Test(priority = 10)
  public void testMethodC(String suiteName, String testName, String sleepFor)
      throws InterruptedException {
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
            .addData(METHODS_DEPENDED_ON, new String[0])
            .addData(GROUPS_BELONGING_TO, new String[0])
            .build());

    TimeUnit.MILLISECONDS.sleep(Integer.parseInt(sleepFor));
  }
}
