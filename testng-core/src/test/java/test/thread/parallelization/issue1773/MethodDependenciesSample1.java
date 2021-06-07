package test.thread.parallelization.issue1773;

import org.testng.Reporter;
import org.testng.annotations.Test;

public class MethodDependenciesSample1 {

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
        testname, MethodDependenciesSample1.class, Thread.currentThread().getId());
  }
}
