package test.listeners;

import java.util.List;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;

public class MyMethodInterceptor implements IMethodInterceptor {

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
