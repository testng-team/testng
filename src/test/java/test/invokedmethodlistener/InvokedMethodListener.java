package test.invokedmethodlistener;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

import java.util.ArrayList;
import java.util.List;

public class InvokedMethodListener implements IInvokedMethodListener {

  public static List<IInvokedMethod> m_methods = new ArrayList<>();

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    m_methods.add(method);
  }

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
  }

}
