package org.testng;

import org.testng.xml.XmlTest;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/** A factory for TestRunners to be used by SuiteRunners. */
public interface ITestRunnerFactory {

  TestRunner newTestRunner(
      ISuite suite,
      XmlTest test,
      Collection<IInvokedMethodListener> listeners,
      List<IClassListener> classListeners);

  /**
   * Produces a new {@link TestRunner}
   *
   * @param suite - The {@link ISuite} object that represents a particular &lt;suite&gt;.
   * @param test - The {@link XmlTest} object that represents a particular &lt;test&gt;.
   * @param listeners - A list of {@link IInvokedMethodListener} listeners.
   * @param classListeners - A list of {@link IClassListener} listeners.
   * @param dataProviderListeners - A Map of {@link IDataProviderListener} listeners.
   * @return - A {@link TestRunner} object.
   */
  default TestRunner newTestRunner(
      ISuite suite,
      XmlTest test,
      Collection<IInvokedMethodListener> listeners,
      List<IClassListener> classListeners,
      Map<Class<? extends IDataProviderListener>, IDataProviderListener> dataProviderListeners) {
    return newTestRunner(suite, test, listeners, classListeners);
  }
}
