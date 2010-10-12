package org.testng.junit;

import org.testng.ITestNGMethod;
import org.testng.internal.ITestResultNotifier;

import java.util.List;


/**
 * An abstraction interface over JUnit test runners.
 *
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public interface IJUnitTestRunner {
  void setTestResultNotifier(ITestResultNotifier notifier);

  void run(Class junitTestClass);

  List<ITestNGMethod> getTestMethods();

}
