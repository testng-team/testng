package test.dataprovider.issue3045;

import java.util.ArrayList;
import java.util.List;
import org.testng.IDataProviderListener;
import org.testng.IDataProviderMethod;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

public class DataProviderListener implements IDataProviderListener {

  public static List<String> logs = new ArrayList<>();

  @Override
  public void beforeDataProviderExecution(
      IDataProviderMethod dataProviderMethod, ITestNGMethod method, ITestContext iTestContext) {
    logs.add(
        testName(iTestContext)
            + "-beforeDataProviderExecution-"
            + dataProviderMethod.getMethod().getName());
  }

  @Override
  public void afterDataProviderExecution(
      IDataProviderMethod dataProviderMethod, ITestNGMethod method, ITestContext iTestContext) {
    logs.add(
        testName(iTestContext)
            + "-afterDataProviderExecution-"
            + dataProviderMethod.getMethod().getName());
  }

  private static String testName(ITestContext ctx) {
    return "[" + ctx.getName() + "]";
  }
}
