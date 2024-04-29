package test.thread.issue3028;

import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.*;

public class DataDrivenTestSample {

  @Test(dataProvider = "dp")
  public void a(int ignored) {
    log();
  }

  @Test(dataProvider = "dp")
  public void b(int ignored) {
    log();
  }

  private void log() {
    ITestResult itr = Reporter.getCurrentTestResult();
    long id = Thread.currentThread().getId();
    Reporter.log("Running " + itr.toString() + " on Thread " + id);
  }

  @DataProvider(name = "dp", parallel = true)
  public Object[][] getData() {
    return new Object[][] {{1}, {2}, {3}, {4}, {5}};
  }
}
