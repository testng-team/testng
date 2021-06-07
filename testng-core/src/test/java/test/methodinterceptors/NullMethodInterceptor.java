package test.methodinterceptors;

import java.util.ArrayList;
import java.util.List;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;

public class NullMethodInterceptor implements IMethodInterceptor {
  @Override
  public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
    return new ArrayList<>();
  }
}
