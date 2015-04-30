package test.invokedmethodlistener;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.collections.Lists;

import java.util.List;

public class InvokedMethodListener implements IInvokedMethodListener {

  private final List<IInvokedMethod> m_methods = Lists.newArrayList();

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    m_methods.add(method);
  }

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
  }

  public List<IInvokedMethod> getInvokedMethods() {
    return m_methods;
  }
}
