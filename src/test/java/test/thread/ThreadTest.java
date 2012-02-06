package test.thread;

import org.testng.annotations.Test;

import test.BaseTest;

public class ThreadTest extends BaseTest {

  @Test
  public void timeoutAndInvocationCountShouldFail() {
    addClass(ThreadPoolSampleBugTest.class.getName());
    run();
    String[] passed = {
        "shouldPass1", "shouldPass2"
    };
    String[] failed = {
        "shouldFail1", "shouldFail2"
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }
}
