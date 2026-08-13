package org.testng.internal;

import java.util.List;
import org.testng.IConfigurable;
import org.testng.IConfigurationListener;
import org.testng.IExecutionListener;
import org.testng.IExecutorServiceFactory;
import org.testng.IHookable;
import org.testng.IInjectorFactory;
import org.testng.ITestNGListenerFactory;
import org.testng.ITestObjectFactory;
import org.testng.ListenerComparator;
import org.testng.internal.annotations.IAnnotationFinder;

public interface IConfiguration {
  IAnnotationFinder getAnnotationFinder();

  void setAnnotationFinder(IAnnotationFinder finder);

  void setListenerFactory(ITestNGListenerFactory testNGListenerFactory);

  ITestNGListenerFactory getListenerFactory();

  void setListenerComparator(ListenerComparator comparator);

  ListenerComparator getListenerComparator();

  ITestObjectFactory getObjectFactory();

  void setObjectFactory(ITestObjectFactory m_objectFactory);

  IHookable getHookable();

  void setHookable(IHookable h);

  IConfigurable getConfigurable();

  void setConfigurable(IConfigurable c);

  List<IExecutionListener> getExecutionListeners();

  default void addExecutionListener(IExecutionListener l) {}

  default boolean addExecutionListenerIfAbsent(IExecutionListener l) {
    return false;
  }

  List<IConfigurationListener> getConfigurationListeners();

  void addConfigurationListener(IConfigurationListener cl);

  boolean alwaysRunListeners();

  void setAlwaysRunListeners(boolean alwaysRun);

  IInjectorFactory getInjectorFactory();

  IExecutorServiceFactory getExecutorServiceFactory();

  void setExecutorServiceFactory(IExecutorServiceFactory factory);

  void setInjectorFactory(IInjectorFactory factory);

  boolean getOverrideIncludedMethods();

  void setOverrideIncludedMethods(boolean overrideIncludedMethods);

  default void setReportAllDataDrivenTestsAsSkipped(boolean reportAllDataDrivenTestsAsSkipped) {}

  default boolean getReportAllDataDrivenTestsAsSkipped() {
    return false;
  }

  void propagateDataProviderFailureAsTestFailure();

  boolean isPropagateDataProviderFailureAsTestFailure();

  boolean isShareThreadPoolForDataProviders();

  void shareThreadPoolForDataProviders(boolean flag);

  boolean useGlobalThreadPool();

  void shouldUseGlobalThreadPool(boolean flag);

  /**
   * @return - Whether {@code @Factory} produced test class instances should be created lazily
   *     (just-in-time) by default. This is the broadest (runner level) toggle; it is overridden by
   *     the suite level {@code lazy-factory} attribute and by the {@code @Factory(lazy=...)}
   *     annotation. Defaults to {@code false} (eager).
   */
  default boolean isLazyFactoryInstantiation() {
    return false;
  }

  default void setLazyFactoryInstantiation(boolean lazyFactoryInstantiation) {}
}
