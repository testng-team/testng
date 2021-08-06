package test.listeners.github2558;

import org.testng.IClassListener;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestClass;
import org.testng.ITestResult;

public class ClassMethodListenersHolder {

  public static class ClassMethodListenerA implements IClassListener, IInvokedMethodListener {

    @Override
    public void onBeforeClass(ITestClass testClass) {
      CallHolder.addCall(getClass().getName() + ".onBeforeClass()");
    }

    @Override
    public void onAfterClass(ITestClass testClass) {
      CallHolder.addCall(getClass().getName() + ".onBeforeClass()");
    }

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
      if (method.isConfigurationMethod()) {
        return;
      }
      CallHolder.addCall(getClass().getName() + ".beforeInvocation()");
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
      if (method.isConfigurationMethod()) {
        return;
      }
      CallHolder.addCall(getClass().getName() + ".afterInvocation()");
    }
  }

  public static class ClassMethodListenerB implements IClassListener, IInvokedMethodListener {

    @Override
    public void onBeforeClass(ITestClass testClass) {
      CallHolder.addCall(getClass().getName() + ".onBeforeClass()");
    }

    @Override
    public void onAfterClass(ITestClass testClass) {
      CallHolder.addCall(getClass().getName() + ".onBeforeClass()");
    }

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
      if (method.isConfigurationMethod()) {
        return;
      }
      CallHolder.addCall(getClass().getName() + ".beforeInvocation()");
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
      if (method.isConfigurationMethod()) {
        return;
      }
      CallHolder.addCall(getClass().getName() + ".afterInvocation()");
    }
  }
}
