package org.testng.internal;

import java.util.ArrayList;
import java.util.List;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.TestListenerAdapter;

public class MethodMultiplyingInterceptor extends TestListenerAdapter
    implements IMethodInterceptor {
  private int originalMethodCount;
  private int multiplyCount = 0;

  @Override
  public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
    originalMethodCount = methods.size();
    List<IMethodInstance> newMethods = new ArrayList<>();
    for (IMethodInstance method : methods) {
      newMethods.add(method);
      TestClassSample.Occurs occurs =
          method
              .getMethod()
              .getConstructorOrMethod()
              .requireMethod()
              .getAnnotation(TestClassSample.Occurs.class);
      if (occurs == null) {
        continue;
      }
      multiplyCount += occurs.times();
      for (int i = 1; i <= occurs.times(); i++) {
        newMethods.add(method);
      }
    }
    return newMethods;
  }

  public int getOriginalMethodCount() {
    return originalMethodCount;
  }

  public int getMultiplyCount() {
    return multiplyCount;
  }
}
