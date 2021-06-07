package test.listeners.github1490;

import org.testng.IDataProviderListener;
import org.testng.IDataProviderMethod;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

public class DataProviderInfoProvider implements IDataProviderListener {
  public static IDataProviderMethod before;
  public static IDataProviderMethod after;

  @Override
  public void beforeDataProviderExecution(
      IDataProviderMethod dataProviderMethod, ITestNGMethod method, ITestContext iTestContext) {
    before = dataProviderMethod;
  }

  @Override
  public void afterDataProviderExecution(
      IDataProviderMethod dataProviderMethod, ITestNGMethod method, ITestContext iTestContext) {
    after = dataProviderMethod;
  }
}
