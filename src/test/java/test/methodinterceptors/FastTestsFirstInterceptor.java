package test.methodinterceptors;

import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FastTestsFirstInterceptor implements IMethodInterceptor {
  @Override
  public List<IMethodInstance> intercept(List<IMethodInstance> methods,
      ITestContext context)
  {
    List<IMethodInstance> result = new ArrayList<>();
    for (IMethodInstance m : methods) {
      Test test = m.getMethod().getMethod().getAnnotation(Test.class);
      Set<String> groups = new HashSet<>();
      for (String group : test.groups()) {
        groups.add(group);
      }
      if (groups.contains("fast")) {
        result.add(0, m);
      }
      else {
        result.add(m);
      }
    }
    return result;
  }

}
