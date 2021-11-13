package test.listeners;

import java.util.List;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.ITestListener;

/**
 * This listener is used to trigger the bug 1863: IMethodInterceptor will be invoked twice when
 * listener implements both ITestListener and IMethodInterceptor via eclipse execution way
 */
public class InterceptorInvokeTwiceSimulateListener implements ITestListener, IMethodInterceptor {

  private int count = 0;

  @Override
  public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
    count++;
    return methods;
  }

  public int getCount() {
    return count;
  }
}
