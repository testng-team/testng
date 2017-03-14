package org.testng.internal;

import java.util.List;
import java.util.Set;

import org.testng.IConfigurationListener;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.xml.XmlTest;

/**
 * An interface defining the notification for @Test results and also
 * \@Configuration results.
 *
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public interface ITestResultNotifier {

  Set<ITestResult> getPassedTests(ITestNGMethod tm);

  Set<ITestResult> getFailedTests(ITestNGMethod tm);

  Set<ITestResult> getSkippedTests(ITestNGMethod tm);

  void addPassedTest(ITestNGMethod tm, ITestResult tr);

  void addSkippedTest(ITestNGMethod tm, ITestResult tr);

  void addFailedTest(ITestNGMethod tm, ITestResult tr);

  void addFailedButWithinSuccessPercentageTest(ITestNGMethod tm, ITestResult tr);

  void addInvokedMethod(InvokedMethod im);

  XmlTest getTest();

  List<ITestListener> getTestListeners();

  List<IConfigurationListener> getConfigurationListeners();
}
