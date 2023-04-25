package test.configuration.issue2664;

import java.util.ArrayList;
import java.util.List;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

public class MethodInvocationListener implements IInvokedMethodListener {

  public final List<String> logs = new ArrayList<>();

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    logs.add(method.getTestMethod().getMethodName());
  }

  @Override
  public void afterInvocation(IInvokedMethod iInvokedMethod, ITestResult iTestResult) {}
}
