package test.methodinterceptors.multipleinterceptors;

import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

import java.util.ArrayList;
import java.util.List;

public abstract class MethodNameFilterInterceptor implements IMethodInterceptor {

  private final String methodName;

  protected MethodNameFilterInterceptor(String methodName) {
    this.methodName = methodName;
  }

  @Override
  public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
    List<IMethodInstance> result = new ArrayList<>();
    for (IMethodInstance methodInstance : methods) {
      ITestNGMethod method = methodInstance.getMethod();
      String name = method.getMethodName();
      if (!name.equals(methodName)) {
        result.add(methodInstance);
        String currentDescription = method.getDescription();
        if (currentDescription == null) {
          method.setDescription(methodName);
        } else {
          method.setDescription(currentDescription + methodName);
        }
      }
    }
    return result;
  }
}
