package org.testng.aftergroups.samples.github1362;

import java.util.ArrayList;
import java.util.List;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;

public class LocalMethodInterceptor implements IMethodInterceptor {
  @Override
  public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
    List<IMethodInstance> methodsToReturn = new ArrayList<>();
    for (IMethodInstance method : methods) {
      if (!method.getMethod().getMethodName().equals("test2")) {
        methodsToReturn.add(method);
      }
    }
    return methodsToReturn;
  }
}
