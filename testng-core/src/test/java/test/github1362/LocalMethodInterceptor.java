package test.github1362;

import java.util.List;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.collections.Lists;

public class LocalMethodInterceptor implements IMethodInterceptor {
  @Override
  public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
    List<IMethodInstance> methodsToReturn = Lists.newArrayList();
    for (IMethodInstance method : methods) {
      if (!method.getMethod().getMethodName().equals("test2")) {
        methodsToReturn.add(method);
      }
    }
    return methodsToReturn;
  }
}
