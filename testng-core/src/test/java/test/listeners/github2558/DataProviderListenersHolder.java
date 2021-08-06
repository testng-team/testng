package test.listeners.github2558;

import org.testng.IDataProviderListener;
import org.testng.IDataProviderMethod;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

public class DataProviderListenersHolder {

  public static class DataProviderListenerA implements IDataProviderListener {

    @Override
    public void beforeDataProviderExecution(
        IDataProviderMethod dataProviderMethod, ITestNGMethod method, ITestContext iTestContext) {
      CallHolder.addCall(getClass().getName() + ".beforeDataProviderExecution()");
    }

    @Override
    public void afterDataProviderExecution(
        IDataProviderMethod dataProviderMethod, ITestNGMethod method, ITestContext iTestContext) {
      CallHolder.addCall(getClass().getName() + ".afterDataProviderExecution()");
    }
  }

  public static class DataProviderListenerB implements IDataProviderListener {

    @Override
    public void beforeDataProviderExecution(
        IDataProviderMethod dataProviderMethod, ITestNGMethod method, ITestContext iTestContext) {
      CallHolder.addCall(getClass().getName() + ".beforeDataProviderExecution()");
    }

    @Override
    public void afterDataProviderExecution(
        IDataProviderMethod dataProviderMethod, ITestNGMethod method, ITestContext iTestContext) {
      CallHolder.addCall(getClass().getName() + ".afterDataProviderExecution()");
    }
  }
}
