package test.listeners.issue3082;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

public class ObjectTrackingMethodListener implements IInvokedMethodListener {

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    String methodName = method.getTestMethod().getMethodName();
    Object instance = method.getTestMethod().getInstance();
    if (instance instanceof IUniqueObject) {
      ObjectRepository.add(((IUniqueObject) instance).id(), methodName);
    }
  }
}
