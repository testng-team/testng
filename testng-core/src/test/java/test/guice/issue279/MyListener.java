package test.guice.issue279;

import com.google.inject.Inject;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.annotations.Guice;

@Guice(moduleFactory = TestDIFactory.class)
public class MyListener implements IInvokedMethodListener {

  @Inject private Greeter greeter;

  private static Greeter instance;

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    instance = greeter;
  }

  static Greeter getInstance() {
    return instance;
  }

  static void clearInstance() {
    instance = null;
  }
}
