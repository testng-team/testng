package test.listeners.github1029;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.xml.XmlTest;

public class Issue1029InvokedMethodListener implements IInvokedMethodListener {
  private final List<String> beforeInvocation = Collections.synchronizedList(new ArrayList<>());
  private final List<String> afterInvocation = Collections.synchronizedList(new ArrayList<>());

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    XmlTest xmlTest = method.getTestMethod().getXmlTest();
    beforeInvocation.add(xmlTest.getName());
  }

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    XmlTest xmlTest = method.getTestMethod().getXmlTest();
    afterInvocation.add(xmlTest.getName());
  }

  public List<String> getAfterInvocation() {
    return afterInvocation;
  }

  public List<String> getBeforeInvocation() {
    return beforeInvocation;
  }
}
