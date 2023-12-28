package test.methodinterceptors;

import java.util.*;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.annotations.Test;

public class FastTestsFirstInterceptor implements IMethodInterceptor {
  @Override
  public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
    List<IMethodInstance> result = new ArrayList<>();
    for (IMethodInstance m : methods) {
      Test test = m.getMethod().getConstructorOrMethod().getMethod().getAnnotation(Test.class);
      Set<String> groups = new HashSet<>(Arrays.asList(test.groups()));
      if (groups.contains("fast")) {
        result.add(0, m);
      } else {
        result.add(m);
      }
    }
    return result;
  }
}
