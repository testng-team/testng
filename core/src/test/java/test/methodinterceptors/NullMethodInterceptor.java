package test.methodinterceptors;

import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;

import java.util.ArrayList;
import java.util.List;

public class NullMethodInterceptor implements IMethodInterceptor {
  @Override
  public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
    return new ArrayList<>();
  }
}
