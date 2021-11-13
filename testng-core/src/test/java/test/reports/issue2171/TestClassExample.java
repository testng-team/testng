package test.reports.issue2171;

import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MyExampleListener.class)
public class TestClassExample {

  @Test
  public void testMethod() {
    ITestResult result = Reporter.getCurrentTestResult();
    result.setAttribute("file", "issue2171.html");
  }
}
