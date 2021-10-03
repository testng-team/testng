package test_result.issue1590;

import java.util.concurrent.TimeUnit;
import org.testng.IInvokedMethod;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestclassSample {
  public static int startStatus;
  public static int endStatus;
  public static long startTimestamp;
  public static long endTimestamp;

  @BeforeClass
  public void beforeClass(ITestContext context) throws InterruptedException {
    TimeUnit.SECONDS.sleep(1);
    ITestResult result = findConfigurationMethod(context).getTestResult();
    startStatus = result.getStatus();
    startTimestamp = result.getEndMillis();
  }

  @Test
  public void testMethod() {}

  @AfterClass
  public void afterClass(ITestContext context) {
    ITestResult result = findConfigurationMethod(context).getTestResult();
    endTimestamp = result.getEndMillis();
    endStatus = ITestResult.SUCCESS;
  }

  private static IInvokedMethod findConfigurationMethod(ITestContext context) {
    return context.getSuite().getAllInvokedMethods().stream()
        .filter(
            iInvokedMethod -> iInvokedMethod.getTestMethod().getMethodName().equals("beforeClass"))
        .findFirst()
        .orElseThrow(IllegalStateException::new);
  }
}
