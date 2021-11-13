package test.listeners;

import java.util.ArrayList;
import java.util.List;
import org.testng.IClassListener;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestClass;
import org.testng.ITestResult;
import org.testng.internal.BaseTestMethod;

public class MyClassListener implements IClassListener, IInvokedMethodListener {

  public static final List<String> names = new ArrayList<>();

  @Override
  public void onBeforeClass(ITestClass testClass) {
    names.add("BeforeClass=" + testClass.getRealClass().getSimpleName());
  }

  @Override
  public void onAfterClass(ITestClass testClass) {
    names.add("AfterClass=" + testClass.getRealClass().getSimpleName());
  }

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    BaseTestMethod m = (BaseTestMethod) method.getTestMethod();
    names.add("BeforeMethod=" + m.getSimpleName());
  }

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    BaseTestMethod m = (BaseTestMethod) method.getTestMethod();
    names.add("AfterMethod=" + m.getSimpleName());
  }
}
