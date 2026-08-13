package test.invokedmethodlistener;

import java.util.ArrayList;
import java.util.List;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

public class InvokedMethodListener implements IInvokedMethodListener {

  private final List<IInvokedMethod> m_methods = new ArrayList<>();

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    m_methods.add(method);
  }

  public List<IInvokedMethod> getInvokedMethods() {
    return m_methods;
  }
}
