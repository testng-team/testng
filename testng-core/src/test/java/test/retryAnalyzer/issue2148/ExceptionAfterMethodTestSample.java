package test.retryAnalyzer.issue2148;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ExceptionAfterMethodTestSample {
  static final AtomicInteger counter = new AtomicInteger(0);
  static final List<String> logs = new ArrayList<>();

  @BeforeMethod(alwaysRun = true)
  public void beforeMethod(ITestResult method) {
    logs.add(
        "Before Method [" + method.getMethod().getMethodName() + "] #" + counter.incrementAndGet());
  }

  @Test(alwaysRun = true, retryAnalyzer = RetryAnalyzer.class)
  public void testMethod() {
    ITestResult method = Reporter.getCurrentTestResult();
    logs.add("Test Method [" + method.getMethod().getMethodName() + "] #" + counter.get());
    Assert.fail();
  }

  @AfterMethod(alwaysRun = true)
  public void afterMethod(ITestResult method) {
    logs.add("Before Method [" + method.getMethod().getMethodName() + "] #" + counter.get());
    throw new RuntimeException("Simulating a failure in AfterMethod");
  }
}
