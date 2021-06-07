package test.listeners.github1490;

import java.util.List;
import org.testng.IDataProviderListener;
import org.testng.IDataProviderMethod;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.collections.Lists;

public class LocalDataProviderListener implements IDataProviderListener {
  public static final List<String> messages = Lists.newArrayList();

  @Override
  public void beforeDataProviderExecution(
      IDataProviderMethod dataProviderMethod, ITestNGMethod method, ITestContext iTestContext) {
    log(method, "before:");
  }

  @Override
  public void afterDataProviderExecution(
      IDataProviderMethod dataProviderMethod, ITestNGMethod method, ITestContext iTestContext) {
    log(method, "after:");
  }

  private static void log(ITestNGMethod method, String prefix) {
    if (method.getInstance() != null) {
      messages.add(
          prefix + method.getInstance().getClass().getName() + "." + method.getMethodName());
    } else {
      messages.add(prefix + method.getMethodName());
    }
  }
}
