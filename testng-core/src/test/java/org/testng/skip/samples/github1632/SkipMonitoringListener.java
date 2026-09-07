package test.skip.github1632;

import java.util.HashMap;
import java.util.Map;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.SkipException;

public class SkipMonitoringListener implements IInvokedMethodListener {

  private final Map<String, Integer> status = new HashMap<>();

  public Map<String, Integer> getStatus() {
    return status;
  }

  @Override
  public void beforeInvocation(IInvokedMethod iInvokedMethod, ITestResult iTestResult) {
    SkipTest skipTest =
        iInvokedMethod
            .getTestMethod()
            .getConstructorOrMethod()
            .getMethod()
            .getAnnotation(SkipTest.class);
    if (skipTest != null) {
      throw new SkipException("skip");
    }
  }

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    status.put(method.getTestMethod().getMethodName(), testResult.getStatus());
  }
}
