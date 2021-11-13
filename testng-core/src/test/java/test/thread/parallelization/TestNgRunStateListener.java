package test.thread.parallelization;

import static test.thread.parallelization.TestNgRunStateTracker.EventInfo.CLASS_INSTANCE;
import static test.thread.parallelization.TestNgRunStateTracker.EventInfo.CLASS_NAME;
import static test.thread.parallelization.TestNgRunStateTracker.EventInfo.GROUPS_BELONGING_TO;
import static test.thread.parallelization.TestNgRunStateTracker.EventInfo.GROUPS_DEPENDED_ON;
import static test.thread.parallelization.TestNgRunStateTracker.EventInfo.METHODS_DEPENDED_ON;
import static test.thread.parallelization.TestNgRunStateTracker.EventInfo.METHOD_NAME;
import static test.thread.parallelization.TestNgRunStateTracker.EventInfo.SUITE_NAME;
import static test.thread.parallelization.TestNgRunStateTracker.EventInfo.TEST_NAME;
import static test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent.LISTENER_SUITE_FINISH;
import static test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent.LISTENER_SUITE_START;
import static test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent.LISTENER_TEST_FINISH;
import static test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent.LISTENER_TEST_METHOD_FAIL;
import static test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent.LISTENER_TEST_METHOD_FAIL_PERCENTAGE;
import static test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent.LISTENER_TEST_METHOD_PASS;
import static test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent.LISTENER_TEST_METHOD_SKIPPED;
import static test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent.LISTENER_TEST_METHOD_START;
import static test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent.LISTENER_TEST_START;
import static test.thread.parallelization.TestNgRunStateTracker.logEvent;

import java.util.concurrent.TimeUnit;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import test.thread.parallelization.TestNgRunStateTracker.EventLog;
import test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent;

public class TestNgRunStateListener implements ISuiteListener, ITestListener {

  @Override
  public void onStart(ISuite suite) {
    logEvent(buildEventLog(suite, LISTENER_SUITE_START).build());
    delayAfterEvent(LISTENER_SUITE_START);
  }

  @Override
  public void onFinish(ISuite suite) {
    logEvent(buildEventLog(suite, LISTENER_SUITE_FINISH).build());
    delayAfterEvent(LISTENER_SUITE_FINISH);
  }

  @Override
  public void onStart(ITestContext context) {
    logEvent(buildEventLog(context, LISTENER_TEST_START).build());
    delayAfterEvent(LISTENER_TEST_START);
  }

  @Override
  public void onFinish(ITestContext context) {
    logEvent(buildEventLog(context, LISTENER_TEST_FINISH).build());
    delayAfterEvent(LISTENER_TEST_FINISH);
  }

  @Override
  public void onTestStart(ITestResult result) {
    logEvent(buildEventLog(result, LISTENER_TEST_METHOD_START).build());
    delayAfterEvent(LISTENER_TEST_METHOD_START);
  }

  @Override
  public void onTestSuccess(ITestResult result) {
    logEvent(buildEventLog(result, LISTENER_TEST_METHOD_PASS).build());
    delayAfterEvent(LISTENER_TEST_METHOD_PASS);
  }

  @Override
  public void onTestFailure(ITestResult result) {
    logEvent(buildEventLog(result, LISTENER_TEST_METHOD_FAIL).build());
    delayAfterEvent(LISTENER_TEST_METHOD_FAIL);
  }

  @Override
  public void onTestSkipped(ITestResult result) {
    logEvent(buildEventLog(result, LISTENER_TEST_METHOD_SKIPPED).build());
    delayAfterEvent(LISTENER_TEST_METHOD_SKIPPED);
  }

  @Override
  public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    logEvent(buildEventLog(result, LISTENER_TEST_METHOD_FAIL_PERCENTAGE).build());
    delayAfterEvent(LISTENER_TEST_METHOD_FAIL_PERCENTAGE);
  }

  private TestNgRunStateTracker.EventLogBuilder buildEventLog(ISuite suite, TestNgRunEvent event) {
    long time = System.currentTimeMillis();

    return EventLog.builder()
        .setEvent(event)
        .setTimeOfEvent(time)
        .setThread(Thread.currentThread())
        .addData(SUITE_NAME, suite.getName());
  }

  private TestNgRunStateTracker.EventLogBuilder buildEventLog(
      ITestContext context, TestNgRunEvent event) {
    return buildEventLog(context.getSuite(), event).addData(TEST_NAME, context.getName());
  }

  private TestNgRunStateTracker.EventLogBuilder buildEventLog(
      ITestResult result, TestNgRunEvent event) {

    return (buildEventLog(result.getTestContext(), event))
        .addData(METHOD_NAME, result.getMethod().getMethodName())
        .addData(CLASS_NAME, result.getMethod().getRealClass().getCanonicalName())
        .addData(CLASS_INSTANCE, result.getMethod().getInstance())
        .addData(GROUPS_DEPENDED_ON, result.getMethod().getGroupsDependedUpon())
        .addData(METHODS_DEPENDED_ON, result.getMethod().getMethodsDependedUpon())
        .addData(GROUPS_BELONGING_TO, result.getMethod().getGroups());
  }

  private void delayAfterEvent(TestNgRunEvent event) {
    try {
      TimeUnit.MILLISECONDS.sleep(500);
    } catch (InterruptedException e) {
      throw new RuntimeException("Problem with delaying after listener event: " + event, e);
    }
  }
}
