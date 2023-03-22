package test.dataprovider.issue2888;

import java.util.Arrays;
import org.testng.IDataProviderListener;
import org.testng.IDataProviderMethod;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.SkipException;

public class SkipDataProviderListener implements ITestListener, IDataProviderListener {
  @Override
  public void onTestStart(ITestResult result) {
    skipIfSkipMe(result.getMethod());
  }

  @Override
  public void onTestSkipped(ITestResult result) {}

  @Override
  public void beforeDataProviderExecution(
      IDataProviderMethod dataProviderMethod, ITestNGMethod method, ITestContext iTestContext) {
    skipIfSkipMe(method);
  }

  private static void skipIfSkipMe(ITestNGMethod testNGMethod) {
    if (Arrays.asList(testNGMethod.getGroups()).contains("SkipMe"))
      throw new SkipException("Test was skipped");
  }
}
