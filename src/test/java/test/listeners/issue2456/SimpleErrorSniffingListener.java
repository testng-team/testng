package test.listeners.issue2456;

import java.lang.reflect.Method;
import org.testng.IDataProviderListener;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

public class SimpleErrorSniffingListener implements IDataProviderListener {

  private Method testMethod;
  private Method dataProvider;
  private Throwable exception;

  @Override
  public void onDataProviderFailure(ITestNGMethod method, ITestContext ctx, RuntimeException t) {
    this.testMethod = method.getConstructorOrMethod().getMethod();
    this.dataProvider = method.getDataProviderMethod().getMethod();
    this.exception = t.getCause();
  }

  public Method getDataProvider() {
    return dataProvider;
  }

  public Method getTestMethod() {
    return testMethod;
  }

  public Throwable getException() {
    return exception;
  }
}
