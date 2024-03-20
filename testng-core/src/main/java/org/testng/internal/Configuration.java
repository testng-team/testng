package org.testng.internal;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;
import org.testng.IConfigurable;
import org.testng.IConfigurationListener;
import org.testng.IExecutionListener;
import org.testng.IExecutorServiceFactory;
import org.testng.IHookable;
import org.testng.IInjectorFactory;
import org.testng.ITestNGListenerFactory;
import org.testng.ITestObjectFactory;
import org.testng.ListenerComparator;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.internal.annotations.DefaultAnnotationTransformer;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.JDK15AnnotationFinder;
import org.testng.internal.objects.GuiceBackedInjectorFactory;

public class Configuration implements IConfiguration {

  private IAnnotationFinder m_annotationFinder;
  private ITestObjectFactory m_objectFactory;
  private IHookable m_hookable;
  private IConfigurable m_configurable;

  private ITestNGListenerFactory m_listenerFactory;

  private boolean shareThreadPoolForDataProviders = false;
  private final Map<Class<? extends IExecutionListener>, IExecutionListener> m_executionListeners =
      Maps.newLinkedHashMap();
  private final Map<Class<? extends IConfigurationListener>, IConfigurationListener>
      m_configurationListeners = Maps.newLinkedHashMap();
  private boolean alwaysRunListeners = true;
  private volatile IExecutorServiceFactory executorServiceFactory = ThreadPoolExecutor::new;

  private IInjectorFactory injectorFactory = new GuiceBackedInjectorFactory();

  private ListenerComparator listenerComparator;
  private boolean overrideIncludedMethods = false;

  private boolean includeAllDataDrivenTestsWhenSkipping;

  private boolean propagateDataProviderFailureAsTestFailure;

  private boolean useGlobalThreadPool = false;

  public Configuration() {
    init(new JDK15AnnotationFinder(new DefaultAnnotationTransformer()));
  }

  public Configuration(IAnnotationFinder finder) {
    init(finder);
  }

  private void init(IAnnotationFinder finder) {
    m_annotationFinder = finder;
  }

  @Override
  public IAnnotationFinder getAnnotationFinder() {
    return m_annotationFinder;
  }

  @Override
  public void setAnnotationFinder(IAnnotationFinder finder) {
    m_annotationFinder = finder;
  }

  @Override
  public void setListenerFactory(ITestNGListenerFactory testNGListenerFactory) {
    this.m_listenerFactory = testNGListenerFactory;
  }

  @Override
  public ITestNGListenerFactory getListenerFactory() {
    return m_listenerFactory;
  }

  @Override
  public void setListenerComparator(ListenerComparator comparator) {
    this.listenerComparator = comparator;
  }

  @Override
  public ListenerComparator getListenerComparator() {
    return listenerComparator;
  }

  @Override
  public ITestObjectFactory getObjectFactory() {
    return m_objectFactory;
  }

  @Override
  public void setObjectFactory(ITestObjectFactory factory) {
    m_objectFactory = factory;
  }

  @Override
  public IHookable getHookable() {
    return m_hookable;
  }

  @Override
  public void setHookable(IHookable h) {
    m_hookable = h;
  }

  @Override
  public IConfigurable getConfigurable() {
    return m_configurable;
  }

  @Override
  public void setConfigurable(IConfigurable c) {
    m_configurable = c;
  }

  @Override
  public List<IExecutionListener> getExecutionListeners() {
    return Lists.newArrayList(m_executionListeners.values());
  }

  @Override
  public boolean addExecutionListenerIfAbsent(IExecutionListener l) {
    return m_executionListeners.putIfAbsent(l.getClass(), l) == null;
  }

  @Override
  public List<IConfigurationListener> getConfigurationListeners() {
    return Lists.newArrayList(m_configurationListeners.values());
  }

  @Override
  public void addConfigurationListener(IConfigurationListener cl) {
    m_configurationListeners.put(cl.getClass(), cl);
  }

  @Override
  public void setAlwaysRunListeners(boolean alwaysRunListeners) {
    this.alwaysRunListeners = alwaysRunListeners;
  }

  @Override
  public void setExecutorServiceFactory(IExecutorServiceFactory executorServiceFactory) {
    this.executorServiceFactory = Objects.requireNonNull(executorServiceFactory);
  }

  @Override
  public IExecutorServiceFactory getExecutorServiceFactory() {
    return executorServiceFactory;
  }

  @Override
  public boolean alwaysRunListeners() {
    return alwaysRunListeners;
  }

  @Override
  public void setInjectorFactory(IInjectorFactory factory) {
    this.injectorFactory = factory;
  }

  @Override
  public IInjectorFactory getInjectorFactory() {
    return this.injectorFactory;
  }

  @Override
  public boolean getOverrideIncludedMethods() {
    return this.overrideIncludedMethods;
  }

  @Override
  public void setOverrideIncludedMethods(boolean overrideIncludedMethods) {
    this.overrideIncludedMethods = overrideIncludedMethods;
  }

  @Override
  public void setReportAllDataDrivenTestsAsSkipped(boolean reportAllDataDrivenTestsAsSkipped) {
    this.includeAllDataDrivenTestsWhenSkipping = reportAllDataDrivenTestsAsSkipped;
  }

  @Override
  public boolean getReportAllDataDrivenTestsAsSkipped() {
    return this.includeAllDataDrivenTestsWhenSkipping;
  }

  @Override
  public void propagateDataProviderFailureAsTestFailure() {
    this.propagateDataProviderFailureAsTestFailure = true;
  }

  @Override
  public boolean isPropagateDataProviderFailureAsTestFailure() {
    return propagateDataProviderFailureAsTestFailure;
  }

  @Override
  public boolean isShareThreadPoolForDataProviders() {
    return this.shareThreadPoolForDataProviders;
  }

  @Override
  public void shareThreadPoolForDataProviders(boolean flag) {
    this.shareThreadPoolForDataProviders = flag;
  }

  @Override
  public boolean useGlobalThreadPool() {
    return this.useGlobalThreadPool;
  }

  @Override
  public void shouldUseGlobalThreadPool(boolean flag) {
    this.useGlobalThreadPool = flag;
  }
}
