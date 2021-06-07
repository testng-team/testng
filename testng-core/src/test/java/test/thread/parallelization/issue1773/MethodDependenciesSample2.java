package test.thread.parallelization.issue1773;

import org.testng.Reporter;
import org.testng.annotations.Test;

public class MethodDependenciesSample2 {

  @Test
  public void parentMethod() {
    log();
  }

  @Test(dependsOnMethods = "parentMethod")
  public void childMethod() {
    log();
  }

  private void log() {
    String testname = Reporter.getCurrentTestResult().getTestContext().getName();
    LogGatheringListener.addLog(
        testname, MethodDependenciesSample2.class, Thread.currentThread().getId());
  }
}
