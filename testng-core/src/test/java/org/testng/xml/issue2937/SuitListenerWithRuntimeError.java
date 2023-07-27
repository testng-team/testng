package org.testng.xml.issue2937;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.testng.IAlterSuiteListener;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

public class SuitListenerWithRuntimeError
    implements IInvokedMethodListener, IAlterSuiteListener, ITestListener {

  public SuitListenerWithRuntimeError() {
    throw new RuntimeException();
  }

  private final Map<String, Set<Long>> mappings = new ConcurrentHashMap<>();

  @Override
  public void alter(List<XmlSuite> suites) {
    XmlSuite xmlSuite = suites.get(0);
    XmlTest xmlTest = xmlSuite.getTests().get(0);
    XmlTest clonedTest = (XmlTest) xmlTest.clone();
    clonedTest.setName(xmlTest.getName() + "_cloned");
  }

  @Override
  public void onStart(ITestContext context) {
    mappings.put(context.getName(), Collections.synchronizedSet(new HashSet<>()));
  }

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    Set<Long> threadIds = mappings.get(testResult.getTestContext().getName());
    threadIds.add(Thread.currentThread().getId());
  }

  public Set<Long> getThreadIds(String testName) {
    return mappings.get(testName);
  }
}
