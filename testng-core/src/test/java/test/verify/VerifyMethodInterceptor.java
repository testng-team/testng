package test.verify;

import java.util.List;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.collections.Lists;

public class VerifyMethodInterceptor implements IMethodInterceptor {
  @Override
  public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
    List<IMethodInstance> result = Lists.newArrayList();
    IMethodInstance verifier = null;

    // Doing a naive approach here: we run through the list of methods
    // twice, once to find the verifier and once more to actually create
    // the result. Obviously, this can be done with just one loop
    for (IMethodInstance m : methods) {
      if (m.getMethod().getConstructorOrMethod().getMethod().getAnnotation(Verifier.class)
          != null) {
        verifier = m;
        break;
      }
    }

    // Create the result with each @Verify method followed by a call
    // to the @Verifier method
    for (IMethodInstance m : methods) {
      if (m != verifier) {
        result.add(m);
      }

      if (m.getMethod().getConstructorOrMethod().getMethod().getAnnotation(Verify.class) != null) {
        result.add(verifier);
      }
    }

    return result;
  }
}
