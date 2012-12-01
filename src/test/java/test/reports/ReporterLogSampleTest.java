package test.reports;

import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.TestListenerAdapter;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import test.reports.ReporterLogSampleTest.MyTestListener;

@Listeners(MyTestListener.class)
public class ReporterLogSampleTest {
  public static class MyTestListener extends TestListenerAdapter {
    @Override
    public void onTestSuccess(ITestResult result) {
      Reporter.log("Log from listener");
      super.onTestSuccess(result);
    }
  }

  @Test
  public void test1() {
    Reporter.log("Log from test method");
  }
}