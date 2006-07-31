package org.testng.internal;

import java.util.List;
import java.util.Set;

import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.xml.XmlTest;

/**
 * This class
 * 
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public interface ITestResultNotifier {
  
  public Set<ITestResult> getPassedTests(ITestNGMethod tm);
  
  public void addPassedTest(ITestNGMethod tm, ITestResult tr);  
  
  public void addSkippedTest(ITestNGMethod tm, ITestResult tr);
  
  public void addFailedTest(ITestNGMethod tm, ITestResult tr);

  public void addFailedButWithinSuccessPercentageTest(ITestNGMethod tm, ITestResult tr);

  public void addInvokedMethod(InvokedMethod im);
  
  public XmlTest getTest();
  
  public List<ITestListener> getTestListeners();
}
