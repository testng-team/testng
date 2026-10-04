package org.testng;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.testng.xml.XmlTest;

/**
 * Creates the {@link TestRunner} for each {@code <test>} of a suite.
 *
 * <p>{@link SuiteRunner} calls the factory once for each {@code <test>}.
 */
public interface ITestRunnerFactory {

  /**
   * Creates a new {@link TestRunner}.
   *
   * @param suite the {@link ISuite} that stands for a {@code <suite>}.
   * @param test the {@link XmlTest} that stands for a {@code <test>}.
   * @param listeners the {@link IInvokedMethodListener} listeners.
   * @param classListeners the {@link IClassListener} listeners.
   * @return the new {@link TestRunner}.
   */
  TestRunner newTestRunner(
      ISuite suite,
      XmlTest test,
      Collection<IInvokedMethodListener> listeners,
      List<IClassListener> classListeners);

  /**
   * Creates a new {@link TestRunner} that also has data provider listeners.
   *
   * <p>The default implementation ignores {@code dataProviderListeners}. It calls {@link
   * #newTestRunner(ISuite, XmlTest, Collection, List)}.
   *
   * @param suite the {@link ISuite} that stands for a {@code <suite>}.
   * @param test the {@link XmlTest} that stands for a {@code <test>}.
   * @param listeners the {@link IInvokedMethodListener} listeners.
   * @param classListeners the {@link IClassListener} listeners.
   * @param dataProviderListeners the {@link IDataProviderListener} listeners, by class.
   * @return the new {@link TestRunner}.
   */
  default TestRunner newTestRunner(
      ISuite suite,
      XmlTest test,
      Collection<IInvokedMethodListener> listeners,
      List<IClassListener> classListeners,
      Map<Class<? extends IDataProviderListener>, IDataProviderListener> dataProviderListeners) {
    return newTestRunner(suite, test, listeners, classListeners);
  }

  /**
   * Creates a new {@link TestRunner} that also has data provider listeners and interceptors.
   *
   * <p>{@link SuiteRunner} calls this method. The default implementation ignores {@code holder}. It
   * calls {@link #newTestRunner(ISuite, XmlTest, Collection, List)}.
   *
   * @param suite the {@link ISuite} that stands for a {@code <suite>}.
   * @param test the {@link XmlTest} that stands for a {@code <test>}.
   * @param listeners the {@link IInvokedMethodListener} listeners.
   * @param classListeners the {@link IClassListener} listeners.
   * @param holder the data provider listeners and interceptors.
   * @return the new {@link TestRunner}.
   */
  default TestRunner newTestRunner(
      ISuite suite,
      XmlTest test,
      Collection<IInvokedMethodListener> listeners,
      List<IClassListener> classListeners,
      DataProviderHolder holder) {
    return newTestRunner(suite, test, listeners, classListeners);
  }
}
