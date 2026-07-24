package test.listeners;

import java.util.List;
import org.testng.*;

public class SuiteListener2
    implements IAnnotationTransformer,
        IInvokedMethodListener,
        ITestListener,
        ISuiteListener,
        IExecutionListener,
        IMethodInterceptor {
  public static int start;
  public static int finish;

  @Override
  public void onFinish(ISuite suite) {
    finish++;
  }

  @Override
  public void onStart(ISuite suite) {
    start++;
  }

  @Override
  public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
    return methods;
  }
}
